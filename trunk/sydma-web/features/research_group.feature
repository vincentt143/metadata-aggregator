@passing @dc2d @dc2f
Feature: Research Group feature

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test" with principal investigator "ictintersect3"
	And user "ictintersect2" has full access to group "Research Group Test"

Scenario: UniKey researchers can be set as DMC
	Given I log in as "ictintersect2"
	And I am on the page for research group "Research Group Test"
	Given I fill in "dataManagementContact" with "ictintersect4" 
	And I press "submit-form"
	Then I should be on the browse rds page	

Scenario: View Access can only see View Group
	Given user "ictintersect3" has viewing access to group "Research Group Test"
	And I log in as "ictintersect3"
	And I select option "Research Group Test" from select "Research_Groups"		
	And I should see "View Group Metadata"
	When I am on the view group page for "Research Group Test"
	Then I should be on the view group page for "Research Group Test"

Scenario: Full Access cannot see View Group
	Given I log in as "ictintersect2"
	And I am on the page for research group "Research Group Test"
	Given I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"	
	Then I should not see "View Group Metadata"	
	When I am on the view group page for "Research Group Test"
	Then I should see "Access Denied"