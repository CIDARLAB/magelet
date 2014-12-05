 #!/bin/perl
 # Written by Harris H. Wang, Church Lab, HMS
 # File name: optMAGE.pl
 # Created: 5/04/2009
 # Last modified: 5/04/2009
 #
 # Description: Designs oligo and primers for MAGE experiments

 use Bio::SeqIO;
 use Bio::Perl;
 use Bio::Location::Simple;
 use POSIX;
 
# open(INFILE2,  "INPUTtarg_verif.txt") or die "Can't open file: $!";
# open(OUTFILE2, ">>OUTprimers.txt") or die "Can't open output: $!";

# print "Option 1: generate oligo file only\nOption 2: generate PCR & sequencing verification file only\nOption 3: generate all files\n";
# print "Enter Option: ";
# $option = <>;

 print "OptMAGE 0.9beta\n";
 print "By Harris Wang\n";
 print "Copyright (C) 2009\n";
 print "Harvard Medical School\n";
 print "Boston, MA 02115, USA\n";

 print "\nloading genome sequence...\n";
 $seqio_obj = Bio::SeqIO->new(-file => "genome.fasta", -format => "fasta");
 $seq_obj = $seqio_obj->next_seq;

 print "loading INPUTparam.txt...\n";
 open(INPARAM,  "INPUTparam.txt") or die "Error! Can't open file: $!";
 foreach (<INPARAM>) {
    if ($_ =~ /^(\d+)\t(\S+)\t(\d+)\t(\d+)\t(\d+)/) {
        $oligosize = $1;
        $dGssthresh = $2;     # threshold for dG, a number less than the threshold will mean shifting the oligo for optimization
        $mloc_dft = $3;       # distance (bps) of mismatch to the 3' end of the oligo
        $mloc_max = $4,       # max amount of basepair shift in the oligo (15 = 15 bps from the 3' of the oligo)
        $cmod = $5;           # number of terminal 5' phosphorothioate bonds;
        $calcreplic = $6;     # automatically calculate replichore information: 0 = no, 1 = yes
        $dGssmin_dft = -200;
    }
 }
 close(INPPARAM);

 my @OriC = (3932974,3933205);
 my @dif = (1597981,1598008);

 print "loading INPUTtarg.txt...\n";
 open(INTARG,  "INPUTtarg.txt") or die "Error! Can't open file: $!";
 foreach (<INTARG>) {
    if ($_ =~ /^(\w+)\t(\S+)\t(\d+)\t(\d+)\t(\d+)\t(\w+)\t(\w+)/) {
        push(@id,"$1");
        push(@strand,"$2");
        if ($calcreplic == 0) { push(@rep,"$3"); }
        else {
            push(@rep,"1") if (($5-$4)/2+$4 < $dif[0] || ($5-$4)/2+$4 > $OriC[1]);
            push(@rep,"2") if (($5-$4)/2+$4 > $dif[1] && ($5-$4)/2+$4 < $OriC[0]);
        }
        push(@start,"$4"-1);##hack to make the indexes inclusive
        push(@end,"$5"+1);##also to go from exclusive to inclusive
        push(@mut,"$6");
        push(@mutseq,"$7");
        if ($4>$5) { die "Error! ID: $1 - start coord ($4) is greater than end coord ($5)"; }
        if (length($7)/2 > ($oligosize/2-$mloc_max))  { die "Error! ID: $1 - mutation length for $$7 exceeds mloc_max ($mloc_max) constraint"; }
    }
 }
 close(INTARG);

 open(OUTOLIGOS, ">>OUToligos.txt") or die "Error! Can't open output: $!";
 print OUTOLIGOS "ID\tSTART\tEND\tSTRAND\tREP\tMUT\tMSHIFT\tdGss\tOLIGOSIZE\tMM_COUNT\tINS_COUNT\tDEL_COUNT\tPRED_RE\tOLIGO SEQ\n";
 close(OUTOLIGOS);

 for ($i = 0; $i <= $#id; $i++) {
    ### generate info about mutation sequence
    if ($mut[$i] ne 'D') {
        $mut_obj = Bio::Seq->new(-seq => $mutseq[$i], -alphabet => 'dna' );
        $rc_mut_obj = $mut_obj->revcom;
        $curr_mutseq = $mut_obj->seq;
        $curr_rc_mutseq = $rc_mut_obj->seq;
        $Msize = length($mutseq[$i]);
    }
    else  { $Msize=0; }
    $Dsize = $end[$i]-$start[$i]-1;
    
    ### generate info about type of mutation
    if ($Msize >= $Dsize) {
        $ins_ct = $Msize-$Dsize;
        $mm_ct = $Dsize;
        $del_ct = 0;
    }
    else {
        $ins_ct = 0;
        $mm_ct = $Msize;
        $del_ct = $Dsize-$Msize;
    }

    ### calculate homology shift information
    $Hsize = $oligosize - $Msize;
    $H1size = floor($Hsize/2);
    $H2size = ceil($Hsize/2);
    if ($mut[$i] ne 'D') {
        if (length($mutseq[$i])<$mloc_max) { $mloc_max_tp = $mloc_max; }
        else {  $mloc_max_tp = length($mutseq[$i]);   }
        $Mshift = $H1size-$mloc_max_tp;
    }
    else { $Mshift = $H1size-$mloc_max; }
    
    ### generate homology blocks
    if ($rep[$i] =~ "1") {
        $block1 = Bio::Location::Simple->new(-start=>($start[$i]-$H1size+1), -end=>($start[$i]), -strand=>"-1");
        $block2 = Bio::Location::Simple->new(-start=>($end[$i]), -end=>($end[$i]+$H2size-1+$Mshift), -strand=>"-1");
        $block1_seq = $seq_obj->subseq($block1);
        $block2_seq = $seq_obj->subseq($block2);
        if ($mut[$i] ne 'D') {
            if ($strand[$i] =~ m/\+/) { $block = $block2_seq.$curr_rc_mutseq.$block1_seq; }
            if ($strand[$i] =~ m/\-/) { $block = $block2_seq.$curr_mutseq.$block1_seq; }
        }
        else { $block = $block2_seq.$block1_seq; }
    }
    if ($rep[$i] =~ "2") {
        $block1 = Bio::Location::Simple->new(-start=>($start[$i]-$H2size+1-$Mshift), -end=>($start[$i]), -strand=>"+1");
        $block2 = Bio::Location::Simple->new(-start=>($end[$i]), -end=>($end[$i]+$H1size-1), -strand=>"+1");
        $block1_seq = $seq_obj->subseq($block1);
        $block2_seq = $seq_obj->subseq($block2);
        if ($mut[$i] ne 'D') {
            if ($strand[$i] =~ m/\+/) { $block = $block1_seq.$curr_mutseq.$block2_seq; }
            if ($strand[$i] =~ m/\-/) { $block = $block1_seq.$curr_rc_mutseq.$block2_seq; }
        }
        else { $block = $block1_seq.$block2_seq; }
    }
    open(OUTALLDUMP, ">>OUTalldump.txt") or die "Error! Can't open output: $!";
    printf OUTALLDUMP "%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\n", $id[$i], $H1size, $H2size, $Mshift, $block1->start, $block1->end, $block2->start, $block2->end, $block;
    printf "%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\n", $id[$i], $H1size, $H2size, $Mshift, $block1->start, $block1->end, $block2->start, $block2->end, $block;

    ### dGss optimize block sequence
    $eosearch = 0;
    $mloc = $mloc_dft;
    $dGssmin = $dGssmin_dft;
    $string = substr $block, $Mshift, $oligosize;
    $dGss = `hybrid-ss-min --NA=DNA --energyOnly -q $string`;
    $dGss = substr $dGss, 0, -1;
    if ($dGss > $dGssthresh) { $Mshift_min = 0; }
    else {
        $Mshift_tp = $Mshift;
        $Mshift_min = $Mshift;
        do {
            $string = substr $block, $Mshift_tp, $oligosize;
            $dGss = `hybrid-ss-min --NA=DNA --energyOnly -q $string`;
            $dGss = substr $dGss, 0, -1;
            if ($dGss > $dGssmin) { $dGssmin = $dGss; $Mshift_min = $Mshift-$Mshift_tp; }
            if ($eosearch == 1) { $eosearch = 2; }
            if ($Mshift_tp == 0 && $eosearch !=2) { $Mshift_tp = $Mshift-$Mshift_min+1; $eosearch = 1; }
            $temp = $Mshift-$Mshift_tp;
            print OUTALLDUMP "\t\t$dGss\t$temp\t\t\t\t\t$string\n";
#            printf "\t\t$dGss\t$temp\t\t\t\t\t$string\n";
            $Mshift_tp = $Mshift_tp-1;
        } while ($eosearch < 2 && $dGss < $dGssthresh);
    }
    print OUTALLDUMP "Optimized: \t\t$dGss\t$Mshift_min\t\t\t\t\t$string\n";
#    print "Optimized: \t\t$dGss\t$Mshift_min\t\t\t\t\t\t$string\n";
    close(OUTALLDUMP);
    #$dGhyb = `hybrid-min --NA=DNA --energyOnly -q $string $wtstring`;

    ### Predict oligo replacement efficiency
    $RE = PredictRE($dGss,$cmod,$oligosize,$mm_ct,$ins_ct,$del_ct);
    $RE = sprintf("%.2f", $RE);
    ### add phosphorothioate modifications
    for ($j=1;$j<$cmod*2;$j+=2) { substr($string, $j, 0) = '*'; }

    open(OUTOLIGOS, ">>OUToligos.txt") or die "Error! Can't open output: $!";
    print OUTOLIGOS "$id[$i]\t$start[$i]\t$end[$i]\t$strand[$i]\t$rep[$i]\t$mut[$i]\t$Mshift_min\t$dGss\t$oligosize\t$mm_ct\t$ins_ct\t$del_ct\t$RE\t$string\n";
    close(OUTOLIGOS);
 }

 sub PredictRE(){
    my $dGss = shift;
    #my $dGhyb = shift;
    my $cmod = shift;
    my $oligosize = shift;
    my $mm_ct = shift;
    my $ins_ct = shift;
    my $del_ct = shift;

    my $dGss_idl = -5;
    #my $dGhyb_idl - -117;
    my $cmod_idl = 4;
    my $oligosize_idl = 90;
    my $mm_ct_idl = 1;
    my $ins_ct_idl = 1;
    my $del_ct_idl = 1;
    my $del_ct_idl = 1;

    $comfact = (32-0.991*($dGss_idl-$dGss))*(0.00126*power($cmod,3)-0.0342*power($cmod,2)+0.264*$cmod+0.408)*
        (-0.000000139*power($oligosize,4)+0.0000269*power($oligosize,3)-0.0015*power($oligosize,2)+0.0301*$oligosize);
    $mmfact = exp(-0.135*($mm_ct-$mm_ct_idl));
    $insfact = exp(-0.075*($ins_ct-$ins_ct_idl));
    if ($del_ct<=30) { $delfact = exp(-0.0579*($del_ct-$del_ct_idl)); }
    else { $delfact = exp(-1.37*log($del_ct)/log(10)); }

    $RE = $comfact;
    if ($mm_ct ne 0) { $RE *= $mmfact; }
    if ($ins_ct ne 0) { $RE *= $insfact; }
    if ($del_ct ne 0) { $RE *= $delfact; }

    return ($RE);
 }
    
 sub power {
     local($i,$t);
     local($n, $p) = @_;
     $t = $n;
     for($i = 1; $i < $p; $i++) {
          $t = $t * $n;
     }
     return $t;
 }

