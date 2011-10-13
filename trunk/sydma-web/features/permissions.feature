@passing
Feature: Setting permissions

Background: 
	Given I have the usual users and roles
	And I have a UniKey user "unikey"
	And I have an internal user "external"
	And I have a research group called "Test_Group"
	And user "researcher" has full access to group "Test_Group"
	And I log in as "researcher"
	
Scenario: Clicking permissions link from group actions list
	Given I am on the browse rds page
	And I select option "Test_Group" from select "Research_Groups"	
	And I follow "Assign permissions to other researchers for this group"
	Then I should be on the group permissions page

Scenario: Adding a UniKey user to the permissions list
	Given I am on the permission page for group "Test_Group"
	And I select option "uniKeyUser" from radio group 
	And I fill in "uniKey" with "unikey"
	And I press "addUserToTable"
	Then I should see "unikey" in the cell "Identifier" in the table "grantingPermissions"	

Scenario: Adding an external user to the permissions list
	Given I am on the permission page for group "Test_Group"
	And I select option "externalUser" from radio group 
	And I fill in "email" with "external"
	And I press "addUserToTable"
	Then I should see "external" in the cell "Identifier" in the table "grantingPermissions"	
  	
Scenario: Unknown UniKey
	Given I am on the permission page for group "Test_Group"
	And I select option "uniKeyUser" from radio group
	And I fill in "uniKey" with "fakeunikey"
	And I press "addUserToTable"
	Then I should see an alert that says "User does not exist or has never logged onto this system"	
	
Scenario: Unknown external user	
	Given I am on the permission page for group "Test_Group"
	And I select option "externalUser" from radio group
	And I fill in "email" with "fakeexternal"
	And I press "addUserToTable"
	Then I should see an alert that says "User does not exist or has never logged onto this system"	
	
Scenario: Deleting a user from the table
	Given I am on the permission page for group "Test_Group"
	And I select option "externalUser" from radio group
	And I fill in "email" with "external"
	And I press "addUserToTable"
	Then I should see "external" in the cell "Identifier" in the table "grantingPermissions"	
	And I press button with value "Delete"
	Then I should not see "external" in the cell "Identifier" in the table "grantingPermissions"	
		
Scenario: Navigating to the next page
	Given I am on the permission page for group "Test_Group"
	And I select option "externalUser" from radio group
	And I fill in "email" with "external"
	And I press "addUserToTable"
	Then I should see "external" in the cell "Identifier" in the table "grantingPermissions"	
	And I press "goToLevelSelection"
	Then I should see a "backToUserSelection" button

Scenario: Assign the Permissions
	Given I am on the permission page for group "Test_Group"
	And I select option "externalUser" from radio group
	And I fill in "email" with "external"
	And I press "addUserToTable"
	Then I should see "external" in the cell "Identifier" in the table "grantingPermissions"	
	And I press "goToLevelSelection"
	And I select option "fullAccess" from radio group
	And I press "acceptPermissions"
	And user "external" should have full access to group "Test_Group"
	Then I should be on the view permission page for research group "Test_Group"
	