@passing @dc2d
Feature: DB Instance

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And I have a dbschema with name "schema1"

Scenario: Full Access can create DB instance
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create db instance" page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "Create Database Instance"
	And I select option "schema1" from select "dbSchema"
	And I fill in textarea "description" with "DB description"
	And I press button with value "Create"
	Then I should see "View Database Instance" 
	And I should see db user with full access to dataset "Dataset 1"
	And a db instance should exist in the database for dataset "Dataset 1"
	And db users should exist in the database for dataset "Dataset 1"
	
Scenario: Edit Access cannot create DB instance
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create db instance" page for research dataset "Dataset 1"
	Then I should not see "Create Database Instance"
	And I should see "Access Denied" 				
	
Scenario: Edit Access can view DB instance
	Given user "ictintersect2" has editing access to group "Research Group Test"	
	And dataset "Dataset 1" has dbinstance with schema "schema1"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "View Database Instance"	
	And I should see db user with update access to dataset "Dataset 1"
	
Scenario: View Access can view DB instance
	Given user "ictintersect2" has viewing access to group "Research Group Test"	
	And dataset "Dataset 1" has dbinstance with schema "schema1"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "View Database Instance"
	And I should see db user with view access to dataset "Dataset 1"
	
Scenario: Edit Access can edit DB instance
	Given user "ictintersect2" has editing access to group "Research Group Test"	
	And dataset "Dataset 1" has dbinstance with schema "schema1"
	And I log in as "ictintersect2"
	And I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should see "Edit Description"
	When I follow "Edit Description"
	Then I should see "Edit Database Instance"	
	And I fill in textarea "description" with "New Description"
	And I press button with value "Save"
	Then I should see "View Database Instance"
	And I should see "New Description"	

Scenario: View Access cannot edit DB instance
	Given user "ictintersect2" has viewing access to group "Research Group Test"	
	And dataset "Dataset 1" has dbinstance with schema "schema1"
	And I log in as "ictintersect2"
	And I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Edit Description"		
