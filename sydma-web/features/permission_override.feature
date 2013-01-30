@passing @dc2d		
Feature: Permission Override

Background:    
    Given I have the usual users and roles
	And I have a research group called "Test_Group"
	And the group "Test_Group" has a project called "Test_Project"
	And the project "Test_Project" has a dataset "Test_Dataset"
	And the research group "Test_Group" has directory "Group One Dir"
	And the research dataset "Test_Dataset" has a directory
	And user "ictintersect2" has full access to group "Test_Group"
	And I log in as "ictintersect2"
	And the dataset "Test_Dataset" has directory "source"
	And the dataset "Test_Dataset" has directory "destination"

Scenario: Moving directories should move the permission
	Given user "ictintersect3" has full access to directory "source" in dataset "Test_Dataset"
	And user "ictintersect3" has viewing access to directory "destination" in dataset "Test_Dataset"
	Given I am on the view dataset page for research dataset "Test_Dataset"
	And I drag directory "source" into "destination"
	And I wait for lightbox to fully open
	And I click "confirm_submit" in the ajax lightbox
	And I wait for lightbox to close
	Then user "ictintersect3" should have full access to sub-directory "source" of directory "destination" in dataset "Test_Dataset"
	And user "ictintersect3" should have viewing access to directory "destination" in dataset "Test_Dataset"

Scenario: Moving directories should delete permission if destination already has that permission level
	Given user "ictintersect3" has full access to directory "source" in dataset "Test_Dataset"
	And user "ictintersect3" has full access to directory "destination" in dataset "Test_Dataset"
	Given I am on the view dataset page for research dataset "Test_Dataset"
	And I drag directory "source" into "destination"
	And I wait for lightbox to fully open
	And I click "confirm_submit" in the ajax lightbox
	And I wait for lightbox to close
	Then user "ictintersect3" should not have a permission entry in sub-directory "source" of directory "destination" in dataset "Test_Dataset"
	And user "ictintersect3" should have full access to directory "destination" in dataset "Test_Dataset"
	
	