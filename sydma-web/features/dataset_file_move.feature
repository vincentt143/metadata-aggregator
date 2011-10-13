@passing
Feature: Test moving of dataset

Background: 
	Given I have the usual users and roles	
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the research group "Research Group Test" has directory "Group One Dir"
	And the research dataset "Dataset 1" has a directory
	And I log in as "researcher"
	And the dataset "Dataset 1" has directory "directoryA"
	And the dataset "Dataset 1" has directory "directoryB"
	And the dataset "Dataset 1" has file "testFile.txt" under directory "directoryA"
	And I am on the view dataset page for research dataset "Dataset 1"		
	
Scenario: Moving the file between directories
	When I open directory "directoryA"
	Then I drag file "testFile.txt" into "directoryB"
	Then I click "confirm_submit" in the ajax lightbox
	Then I wait for lightbox to close
	Then I should be able to see file "testFile.txt" in "directoryB"			
	

Scenario: Moving in the same directory should have no effect
	When I open directory "directoryA"
	Then I drag file "testFile.txt" into "directoryA"
	Then I should not see any ajax lightbox
				
Scenario: Moving parent to child should have no effect
	Given the dataset "Dataset 1" has directory "directoryC" under directory "directoryA"
	When I open directory "directoryA"
	Then I drag directory "directoryA" into "directoryC"
	Then I should not see any ajax lightbox
	
Scenario: Moving file to file should have no effect
	Given the dataset "Dataset 1" has file "testFileA.txt" under directory "directoryA"
	Given the dataset "Dataset 1" has file "testFileB.txt" under directory "directoryA"
	When I open directory "directoryA"
	Then I drag directory "testFileA.txt" into "testFileB.txt"
	Then I should not see any ajax lightbox	
								
		

	
	
