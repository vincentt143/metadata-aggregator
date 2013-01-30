Feature: Setting permissions

Background: 
	Given I have the usual users and roles
	And I have a research group called "Test_Group"
	And user "ictintersect2" has full access to group "Test_Group"
	And I log in as "ictintersect2"

@passing @dc2d @dc2f
Scenario: Clicking permissions link from group actions list
	Given I am on the browse rds page
	And I select option "Test_Group" from select "Research_Groups"	
	And I follow "Assign group permissions"
	Then I should see "Grant Permissions"

@passing @dc2d @dc2f
Scenario: Adding a UniKey user to the permissions list
	Given I am on the permission page for group "Test_Group"
	And I fill in "token-input-users" with "ictintersect3"	
	And I click on the dropdown box
	Then I should see "given surname"
	And I select option "editingAccess" from radio group
	And I press button with value "Assign"
	Then I should be on the view permission page for research group "Test_Group"

@passing @dc2d @dc2f
Scenario: Unknown User
	Given I am on the permission page for group "Test_Group"
	And I fill in "token-input-users" with "fakeuser"	
	Then I should see "No results"

@passing @dc2d @dc2f
Scenario: Deleting a user from the table
	Given I am on the permission page for group "Test_Group"
	And I fill in "token-input-users" with "ictintersect2"	
	And I click on the dropdown box
	Then I should see "given surname"
	Then I remove the selected user from the dropdown box
	Then I should not see "given surname"

@passing @dc2d @dc2f
Scenario: Accessing Delete Permissions page with Full Access
	Given user "ictintersect3" has full access to group "Test_Group"
	Given I am on the view permission page for research group "Test_Group"
	And I follow "Delete"
	Then I should be on the delete permission page for user "ictintersect3" under research group "Test_Group"

@dependency @dc2d @dc2f
Scenario: Deleting Permissions
	Given user "ictintersect3" has full access to group "Test_Group"
	When I am on the delete permission page for user "ictintersect3" under research group "Test_Group"
	When I press button with value "Delete"
	Then I should be on the view permission page for research group "Test_Group"
	And I log in as "ictintersect3"
	Then I should not see "Test_Group"