@passing @dc2d @dc2f
Feature: Activity Logging

Background: 
	Given I have the usual users and roles
	And I have a research group called "Test_Group"
	And I log in as "ictintersect2"

Scenario: Can access activity log page as full access
	Given user "ictintersect2" has full access to group "Test_Group"
	Given I am on the browse rds page
	And I select option "Test_Group" from select "Research_Groups"	
	And I follow "Activity Log"
	Then I should see "Activity Log"

Scenario: Can access activity log page as editing access
	Given user "ictintersect2" has editing access to group "Test_Group"
	Given I am on the browse rds page
	And I select option "Test_Group" from select "Research_Groups"	
	And I follow "Activity Log"
	Then I should see "Activity Log"
	
Scenario: Cannot access activity log page as viewing access
	Given user "ictintersect2" has viewing access to group "Test_Group"
	Given I am on the browse rds page
	Then I should not see "Activity Log"