@passing @dc2d @dc2f
Feature: Assigning permissions when you have editing access

Background:
	Given I have the usual users and roles
	And I have a research group called "Test_Group"
	And the group "Test_Group" has a project called "Test_Project"
	And the project "Test_Project" has a dataset "Test_Dataset"
	And the research group "Test_Group" has directory "Group One Dir"
	And the research dataset "Test_Dataset" has a directory
	And user "ictintersect2" has editing access to group "Test_Group"
	And I log in as "ictintersect2"
	And the dataset "Test_Dataset" has directory "Test_Directory"

Scenario: Editing access user can not assign full access for group
	Given I am on the permission page for group "Test_Group"
	Then I should not see "Full Access"

Scenario: Assign editing access to another user for group
	Given I am on the permission page for group "Test_Group"
	And I fill in "token-input-users" with "ictintersect3"	
	And I click on the dropdown box
	Then I should see "given surname"
	And I select option "editingAccess" from radio group
	And I press button with value "Assign"
	Then I should be on the view permission page for research group "Test_Group"
	
Scenario: Editing access user can not assign full access for project
	Given I am on the permission page for project "Test_Project"
	Then I should not see "Full Access"

Scenario: Assign editing access to another user for project
	Given I am on the permission page for project "Test_Project"
	And I fill in "token-input-users" with "ictintersect3"	
	And I click on the dropdown box
	Then I should see "given surname"
	And I select option "editingAccess" from radio group
	And I press button with value "Assign"
	Then I should be on the view permission page for research group "Test_Group"

Scenario: Editing access user can not assign full access for dataset
	Given I am on the permission page for dataset "Test_Dataset"
	Then I should not see "Full Access"

Scenario: Assign editing access to another user for dataset
	Given I am on the permission page for dataset "Test_Dataset"
	And I fill in "token-input-users" with "ictintersect3"	
	And I click on the dropdown box
	Then I should see "given surname"
	And I select option "editingAccess" from radio group
	And I press button with value "Assign"
	Then I should be on the view permission page for research group "Test_Group"

Scenario: Editing access user can not assign editing access for directory
	Given I am on the view dataset page for dataset "Test_Dataset"
	Then I should not see "Full Access"
	Then I should not see "Editing Access"

Scenario: Assign full access to another user for directory
	Given I am on the view dataset page for dataset "Test_Dataset"
	And I select directory "Test_Directory"
	And I follow "Assign permissions"
	And I fill in "token-input-users" with "ictintersect3"	
	And I click on the dropdown box
	Then I should see "given surname"
	When I select option "fullAccess" from radio group
	And I press button with value "Assign"
	Then user "ictintersect3" should have full access to directory "Test_Directory" in dataset "Test_Dataset"
