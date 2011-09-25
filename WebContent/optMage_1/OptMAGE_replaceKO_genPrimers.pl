 #!/bin/perl
 # Written by Harris H. Wang, Church Lab, HMS
 # File name: OptMAGE_genPrimers.pl
 # Created: 01/21/2010
 #
 # Description: Designs PCR primers to verify insert

 use Bio::SeqIO;
 use Bio::Perl;
 use Bio::Location::Simple;

 sub readNNParam;
 sub shortOligoTm;
 sub optimizeTm;

 my $R = 1.987;
 my %deltaH;
 my %deltaS;
 my $C_primer = 200;           # nM
 my $C_Mg = 1.5;                # mM
 my $C_MonovalentIon = 50;    # mM
 my $C_dNTP = 0.4;           	# mM
 my $percentage_DMSO = 0;
 my $percentage_annealed = 50; # percentage of templates that anneal to primers
 my $NNParamFile = "./NN_param.txt";
 readNNParam($NNParamFile);
 my $primersize = 35;
 my $outer_buff = 100;
 my $inner_buff = 200;
 my $targetTm = 62;

 $seqio_obj = Bio::SeqIO->new(-file => "U00096.2.fasta", -format => "fasta");
 open(INFILE,  "INPUT_AA_KOs_v2.txt") or die "Can't open file: $!";
 open(OUTFILE, ">>OUT_AA_KOs_PCRprimers_v2.txt") or die "Can't open output.txt: $!";

 print OUTFILE "ID\tlpos\trpos\tprimer\tsize\tprimer sequence(5'->3')\tTm\n";
 print OUTFILE "outer buffer size = $outer_buff; inner buffer size = $inner_buff\n";

 $seq_obj = $seq_obj = $seqio_obj->next_seq;

 foreach (<INFILE>) {
     if ($_ =~ /^(\w+)\t(\d+)\t(\d+)\t(\w+)\t(\S+)/) {
        $id = $1; $lpos = $2; $rpos = $3; $antibiot = $4; $td = $5;
        if ($td =~ m/\+/) {
            $fpos_start = $lpos-$outer_buff;
            $fpos_end = $lpos-$outer_buff+$primersize;
            $rpos_start = $lpos+$inner_buff-$primersize;
            $rpos_end = $lpos+$inner_buff-1;
            $f_primer = Bio::Location::Simple->new(-start=>$fpos_start, -end=>$fpos_end, -strand=>"1");
            $r_primer = Bio::Location::Simple->new(-start=>$rpos_start, -end=>$rpos_end, -strand=>"-1");
        }
        if ($td =~ m/\-/) {
            $fpos_start = $rpos+$outer_buff-$primersize;
            $fpos_end = $rpos+$outer_buff;
            $rpos_start = $rpos-$inner_buff+1;
            $rpos_end = $rpos-$inner_buff+$primersize;
            $f_primer = Bio::Location::Simple->new(-start=>$fpos_start, -end=>$fpos_end, -strand=>"-1");
            $r_primer = Bio::Location::Simple->new(-start=>$rpos_start, -end=>$rpos_end, -strand=>"+1");
        }
        $f_primerseq = $seq_obj->subseq($f_primer);
        $r_primerseq = $seq_obj->subseq($r_primer);
        ($f_primerseq_opt,$f_primerseq_Tm) = optimizeTm($f_primerseq,1);
        ($r_primerseq_opt,$r_primerseq_Tm) = optimizeTm($r_primerseq,1);
        $flength = length($f_primerseq_opt);
        $rlength = length($r_primerseq_opt);
        $fpos_end = $fpos_start+$flength-1;
        $rpos_start = $rpos_end-$rlength+1;
        print OUTFILE "$id"."_vf\t$fpos_start\t$fpos_end\t$antibiot\t$td\tforward\t$flength\t$f_primerseq_opt\t$f_primerseq_Tm\n";
        print OUTFILE "$id"."_vr\t$rpos_start\t$rpos_end\t$antibiot\t$td\treverse\t$rlength\t$r_primerseq_opt\t$r_primerseq_Tm\n";
        }
 }
 
 sub optimizeTm(){
    my $seq = uc(shift);
    my $chopdir = shift;

    $templength = length($seq);
    for (my $i=0; $i<$templength-1; $i++) {
        if ($chopdir == 0) { #5'->3' chop
            $tempseq = substr $seq, $i;
        }
        else { #3'->5' chop
            $tempseq = substr $seq, 0, -($i+1);
        }
        my $tempTm = shortOligoTm($tempseq, $C_primer, $C_Mg, $C_MonovalentIon, $C_dNTP, $percentage_DMSO, $percentage_annealed);
        push(@allseq,$tempseq);
        push(@allTm,$tempTm);
        push(@deltaTm,abs($tempTm-$targetTm));
        my $tempdeltaTm = abs($tempTm-$targetTm);
   }
    my @sorteddeltaTm = sort @deltaTm;
    for (my $i=0;$i<$templength-1;$i++) {
        if ($sorteddeltaTm[0] == $deltaTm[$i]) {
            $optseq = $allseq[$i];
            $optTm = $allTm[$i];
        }
    }
    undef @allseq;
    undef @allTm;
    undef @deltaTm;
    return ($optseq,$optTm);
 }
 
 
 sub readNNParam(){
	my $NNParamFile = shift;
	open(NNFILE, $NNParamFile) || die("Error in opening NN parameter file!");
	my $line = <NNFILE>;
	while($line = <NNFILE>){
		chop($line);
		my ($seqF, $seqR, $dH, $dS, $mismatch) = split(/[:\t]/, $line);
		if(!$mismatch){
			$deltaH{'pm'}->{$seqF} = $dH;
			$deltaS{'pm'}->{$seqF} = $dS;
		}else{
			$deltaH{'mm'}->{$seqF}->{$seqR} = $dH;
			$deltaS{'mm'}->{$seqF}->{$seqR} = $dS;
		}
	}
	close(NNFILE);
 }

 sub shortOligoTm(){
    my $seq = shift;
    my $C_primer = shift;   # nM
    my $C_Mg = shift;       # mM
    my $C_MonovalentIon = shift;    #mM
		my $C_dNTP = shift;	#mM
		my $percentage_DMSO = shift;
    my $percentage_annealed = shift; #percentage of templates that anneal to primers

    $seq =~ s/[ \t\n]+//g;
    $percentage_annealed = 50.0 if (!$percentage_annealed);
    $percentage_annealed /= 100.0;

    my $C_SodiumEquivalent = $C_MonovalentIon + 120 * sqrt($C_Mg-$C_dNTP);
    my $seqLength = length($seq);
    my $dH = $deltaH{'pm'}->{substr($seq, 0, 1)} + $deltaH{'pm'}->{substr($seq, $seqLength-1, 1)};
    my $dS = $deltaS{'pm'}->{substr($seq, 0, 1)} + $deltaS{'pm'}->{substr($seq, $seqLength-1, 1)};
    $seq = uc($seq);
    for(my $i = 0; $i < $seqLength - 1; $i ++){
        $dH += $deltaH{'pm'}->{substr($seq, $i, 2)};
        $dS += $deltaS{'pm'}->{substr($seq, $i, 2)};
    }
    $dS += 0.368 * $seqLength * log($C_SodiumEquivalent/1000.0);
    my $Tm = sprintf("%5.2f", ($dH * 1000) / ($dS + $R * (log($C_primer*(1-$percentage_annealed)/$percentage_annealed)-21.4164)) - 273.15 - 0.75*$percentage_DMSO);
    return $Tm;
}

