@passing @dc2d @dc2f
Feature: File tree aligned with permissions

Background:
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the project "Research Project Test" has a dataset "Dataset 2"
	And the research group "Research Group Test" has directory "Group One Dir"
	And the research dataset "Dataset 1" has a directory
	And I log in as "ictintersect2"
	And the dataset "Dataset 1" has directory "directoryA"
	And the dataset "Dataset 1" has directory "directoryB"
	And the dataset "Dataset 1" has directory "directoryC"
	And the dataset "Dataset 1" has directory "directoryD"
	And the dataset "Dataset 1" has file "testFile.txt" under directory "directoryA"
	And user "ictintersect2" has full access to directory "directoryA" in dataset "Dataset 1"
	And user "ictintersect2" has viewing access to directory "directoryB" in dataset "Dataset 1"
	And the dataset "Dataset 1" has directory "childDirectory" under directory "directoryD"

Scenario: check inaccessible directories are not shown
	Given user "ictintersect2" has no access to directory "directoryC" in dataset "Dataset 1"
	And I am on the view dataset page for dataset "Dataset 1"
	Then I should see "directoryA"
	And I should see "directoryB"
	And I should not see "directoryC"
	And I should not see "directoryD"
	
Scenario: with access to the group I should see all directories
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I am on the view dataset page for dataset "Dataset 1"
	Then I should see "directoryA"
	And I should see "directoryB"
	And I should see "directoryC"
	And I should see "directoryD"
	
Scenario: directory with explicit NO_ACCESS should not show
	Given user "ictintersect2" has full access to group "Research Group Test"
	And user "ictintersect2" has no access to directory "directoryC" in dataset "Dataset 1"	
	And I am on the view dataset page for dataset "Dataset 1"
	Then I should see "directoryA"
	And I should see "directoryB"
	And I should not see "directoryC"
	And I should see "directoryD"
	
Scenario: permission to child directory shows parent
	Given user "ictintersect2" has full access to sub-directory "childDirectory" of directory "directoryD" in dataset "Dataset 1"
	And I am on the view dataset page for dataset "Dataset 1"
	Then I should see "directoryA"
	And I should see "directoryB"
	And I should not see "directoryC"
	And I should see "directoryD"
	And I open directory "directoryD"
	Then I should see "childDirectory"