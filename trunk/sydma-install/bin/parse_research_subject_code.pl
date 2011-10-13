#!/usr/bin/perl

use Text::Autoformat;

use strict;
use warnings;


sub println
{
	my $msg = shift;
	print $msg."\n";
}

sub read_file
{
	my ($filename) = @_;
	open (my $FILE, "<", $filename) or die $!;
	my @data = <$FILE>;
	close $FILE;
	return \@data;
}

sub output_to_file
{
	my ($filename, $content) = @_;
	open (my $FILE, ">", $filename) or die $!;
	print $FILE $content;
	close $FILE;
}

sub parse_csv
{
	my ($csv_data) = @_;
	println "Reading csv";		
	my $sql_outputs = "";
	foreach my $csv_row (@$csv_data)
	{
		#print "csv row $csv_row";
		
		if ($csv_row =~ /^(\d{6}),(.*),/)
		{
			my $code = $1;
			my $text = $2;
			
			#remove quotatation
			if ($text =~ /^"(.*)"$/)
			{
				$text = $1;
			}
						
			#correct case
			$text = autoformat($text, {case=>'highlight'});
			
			#trim
			$text = trim($text);		
			
			#escape single quotes			
			$text =~ s/'/''/g;
			
			
			my $sql_row	= "INSERT INTO research_subject_code(subject_code,subject_name) VALUES('$code','$text'); ";
			
			$sql_outputs .= $sql_row."\n";
			
			#println "code is $code, text is $text";
		}
		else
		{
			println "row does not conform to expected pattern $csv_row";
		}
	}
	return $sql_outputs;
}

sub execute
{
	my $csv_data = read_file("../resource/research_subject_code.csv");
	
	
	
	
	my $sql_outputs = parse_csv($csv_data);
	
	$sql_outputs = "delete from research_subject_code;\n".$sql_outputs;
	
	output_to_file("../temp/create_research_subject_code.sql", $sql_outputs);
			
}


##MAIN EXECUTION##
execute();




#UTIL FUNCTION
sub trim($)
{
	my $string = shift;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}
# Left trim function to remove leading whitespace
sub ltrim($)
{
	my $string = shift;
	$string =~ s/^\s+//;
	return $string;
}
# Right trim function to remove trailing whitespace
sub rtrim($)
{
	my $string = shift;
	$string =~ s/\s+$//;
	return $string;
}

