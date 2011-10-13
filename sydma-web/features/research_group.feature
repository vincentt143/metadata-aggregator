@passing
Feature: Research Group feature

Background: 
	Given I have the usual users and roles
	And I have a UniKey user "unikey"
	And I have an internal user "external"
	And I have a research group called "Research Group Test"
	And user "researcher" has full access to group "Research Group Test"
	And I log in as "researcher"
	And I am on the page for research group "Research Group Test"

Scenario: UniKey researchers can be set as PI
	Given I fill in "principalInvestigator" with "unikey" 
	And I press "submit"
	Then I should be on the browse rds page	
	
Scenario: Non UniKey researchers cannot be set as PI
	Given I fill in "principalInvestigator" with "external" 
	And I press "submit"
	Then I should see "The Principal Investigator is not a UniKey user, or they have not yet logged in into the Research Data Manager"

Scenario: UniKey researchers can be set as DMC
	Given I fill in "dataManagementContact" with "unikey" 
	Given I fill in "principalInvestigator" with "unikey" 
	And I press "submit"
	Then I should be on the browse rds page	

Scenario: Non UniKey researchers cannot be set as DMC
	Given I fill in "dataManagementContact" with "external" 
	And I press "submit"
	Then I should see "The Data Management Contact is not a UniKey user, or they have not yet logged in into the Research Data Manager"

	
