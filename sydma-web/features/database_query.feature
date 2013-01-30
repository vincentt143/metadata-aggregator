@passing @dc2d
Feature: Research Database Query

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And I have a dbschema with name "schema1"
	And dataset "Dataset 1" has dbinstance with schema "schema1"

Scenario: Full Access can create SQL Query
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I press "new query"
	And I fill in "name" with "Test Query"
	And I fill in textarea "description" with "SQL description"
	And I fill in textarea "query" with "Some SQL Query"
	And I press button with value "Create"
	Then I should see "View Database Instance"
	
Scenario: Edit Access cannot see create SQL Query
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Create Query"

Scenario: Edit Access cannot access create SQL Query
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create sql" page for research dataset "Dataset 1"
	Then I should see "Access Denied"
	
Scenario: View Access cannot create SQL Query
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Create Query"
	
Scenario: View Access cannot access create SQL Query
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create sql" page for research dataset "Dataset 1"
	Then I should see "Access Denied"

Scenario: Show SQL Query
	Given user "ictintersect2" has full access to group "Research Group Test"
	Given I have query "new query" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I follow "Show"
	Then I should be on the show SQL query page for query name "new query" under research dataset "Dataset 1"

Scenario: Running malformed SQL query
	Given user "ictintersect2" has full access to group "Research Group Test"
	Given I have query "malform" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I follow "Run"
	Then I should see "There was an error in the query. Please revise your statement."

Scenario: View access cannot edit SQL query
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	Given I have query "new query" for dataset "Dataset 1"
	When I log in as "ictintersect2"
	When I am on the "edit sql" page for query "new query" under research dataset "Dataset 1"
	Then I should see "Access Denied"

Scenario: Edit access can edit SQL query
	Given user "ictintersect2" has editing access to group "Research Group Test"
	Given I have query "new query" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I follow "Edit"
	And I fill in "name" with "Edited Query"
	And I fill in textarea "description" with "Edited SQL description"
	And I fill in textarea "query" with "Edited SQL Query"
	And I press button with value "Edit"
	And I follow "Show"
	Then I should see "Edited Query"
	And I should see "Edited SQL description"
	And I should see "Edited SQL Query"
	
Scenario: Full access can edit SQL query
	Given user "ictintersect2" has full access to group "Research Group Test"
	Given I have query "new query" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I follow "Edit"
	And I fill in "name" with "Edited Query"
	And I fill in textarea "description" with "Edited SQL description"
	And I fill in textarea "query" with "Edited SQL Query"
	And I press button with value "Edit"
	And I follow "Show"
	Then I should see "Edited Query"
	And I should see "Edited SQL description"
	And I should see "Edited SQL Query"
	
Scenario: Cannot run edit access SQL queries
	Given user "ictintersect2" has editing access to group "Research Group Test"
	Given I have query "editing query" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I follow "Run"
	Then I should see "There was an error in the query. Please revise your statement."

Scenario: Cannot run full access SQL queries
	Given user "ictintersect2" has full access to group "Research Group Test"
	Given I have query "delete query" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I follow "Run"
	Then I should see "There was an error in the query. Please revise your statement."

Scenario: Full access can delete query
	Given user "ictintersect2" has full access to group "Research Group Test"
	Given I have query "show tables" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "delete sql" page for query "show tables" under research dataset "Dataset 1"
	And I press button with value "Delete"
	Then I should be on the "view db instance" page for research dataset "Dataset 1"
	And I should not see "show tables"

Scenario: Edit access cannot delete query
	Given user "ictintersect2" has editing access to group "Research Group Test"
	Given I have query "show tables" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "delete sql" page for query "show tables" under research dataset "Dataset 1"
	Then I should see "Access Denied"

Scenario: View access cannot delete query
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	Given I have query "show tables" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "delete sql" page for query "show tables" under research dataset "Dataset 1"
	Then I should see "Access Denied"
	
Scenario: Download file after query
	Given user "ictintersect2" has full access to group "Research Group Test"
	Given I have a proper query "download" for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	And I follow "Run"
	Then I should get a download with filename "download"
	