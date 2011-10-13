@passing
Feature: Test managing of dataset

Background: 
	Given I have the usual users and roles	
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the research group "Research Group Test" has directory "Group One Dir"
	And the research dataset "Dataset 1" has a directory
	And user "researcher" has full access to group "Research Group Test"
	And I log in as "researcher"
	
Scenario: View dataset link present for dataset
	Given I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	Then I should see "View Dataset"
	
Scenario: Clicking view browse dataset should go to view dataset page
	Given I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "View Dataset"
	Then I should be on the view dataset page for research dataset "Dataset 1"
	
Scenario: Deep browsing files for the dataset
	Given the dataset "Dataset 1" has directory "directoryA"
	Given the dataset "Dataset 1" has file "testFile.txt" under directory "directoryA"
	Given I am on the view dataset page for research dataset "Dataset 1"		
	Then I should be able to see file "directoryA"
	When I open directory "directoryA"
	Then I should be able to see file "testFile.txt"		
		
Scenario: Create Directory
	Given the dataset "Dataset 1" has directory "directoryA"
	Given I am on the view dataset page for research dataset "Dataset 1"	
	When I open directory "directoryA"
	Then I select directory "directoryA"	
	Then I follow "Create Directory"
	When I fill in "directoryName" with "Created Directory"
	Then I press "confirm_submit"	
	Then I should be able to see directory "Created Directory"		
	
Scenario: Delete File
	Given the dataset "Dataset 1" has directory "directoryA"
	Given the dataset "Dataset 1" has file "testFile.txt" under directory "directoryA"
	Given I am on the view dataset page for research dataset "Dataset 1"	
	When I open directory "directoryA"
	Then I select file "testFile.txt"	
	Then I follow "Delete File"	
	Then I press "confirm_submit"	
	Then I should not be able to see file "testFile.txt"			