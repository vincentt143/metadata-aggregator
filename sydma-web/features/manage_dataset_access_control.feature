@passing @dc2d @dc2f
Feature: Test managing of dataset where user only has view access

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the research group "Research Group Test" has directory "Group One Dir"
	And the research dataset "Dataset 1" has a directory
	And user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	
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
		
Scenario: Cannot Create or Delete Directory
	Given the dataset "Dataset 1" has directory "directoryA"
	Given I am on the view dataset page for research dataset "Dataset 1"	
	When I open directory "directoryA"
	Then I select directory "directoryA"	
	Then I should not see "Create Directory"
	Then I should not see "Delete Directory"		
	
Scenario: Cannot Delete File
	Given the dataset "Dataset 1" has directory "directoryA"
	Given the dataset "Dataset 1" has file "testFile.txt" under directory "directoryA"
	Given I am on the view dataset page for research dataset "Dataset 1"	
	When I open directory "directoryA"
	Then I select file "testFile.txt"	
	Then I should not see "Delete File"		

Scenario: Cannot Move Files
	Given the dataset "Dataset 1" has directory "directoryA"
	And the dataset "Dataset 1" has directory "directoryB"
	And the dataset "Dataset 1" has file "testFile.txt" under directory "directoryA"
	And I am on the view dataset page for research dataset "Dataset 1"
	When I open directory "directoryA"
	Then I drag file "testFile.txt" into "directoryB"
	Then I should see "Permission Denied"