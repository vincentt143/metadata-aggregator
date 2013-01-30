@passing @dc2d @dc2f
Feature: Setting project & dataset permissions

Background: 
	Given I have the usual users and roles
	And I have a research group called "Test_Group"
	And the group "Test_Group" has a project called "Test_Project"
	And the project "Test_Project" has a dataset "Test_Dataset"
	And user "ictintersect2" has full access to group "Test_Group"
	And I log in as "ictintersect2"

Scenario: Clicking permissions link from project actions list
	Given I am on the browse rds page
	And I select option "Test_Group" from select "Research_Groups"	
	And I select option "Test_Project" from select "Projects"
	And I follow "Assign project permissions"
	Then I should see "Grant Permissions"

Scenario: Clicking permissions link from dataset actions list
	Given I am on the browse rds page
	And I select option "Test_Group" from select "Research_Groups"	
	And I select option "Test_Project" from select "Projects"
	And I select option "Test_Dataset" from select "Datasets"
	And I follow "Assign dataset permissions"
	Then I should see "Grant Permissions"

Scenario: Adding a user to the permissions list at project level
	Given I am on the permission page for project "Test_Project"
	And I fill in "token-input-users" with "ictintersect3"	
	And I click on the dropdown box
	Then I should see "given surname"
	And I select option "fullAccess" from radio group
	And I press button with value "Assign"
	Then I should be on the view permission page for research group "Test_Group"
	
Scenario: Adding a user to the permissions list at dataset level
	Given I am on the permission page for dataset "Test_Dataset"
	And I fill in "token-input-users" with "ictintersect3"	
	And I click on the dropdown box
	Then I should see "given surname"
	And I select option "fullAccess" from radio group
	And I press button with value "Assign"
	Then I should be on the view permission page for research group "Test_Group"