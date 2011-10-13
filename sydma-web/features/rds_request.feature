Feature: Request RDS space

Background: 
	Given I have the usual users and roles
	And I have the research subject code with code "90" and name "some code"
	And I have a UniKey user "unikey"
	And I have an internal user "external"
	And I log in as "researcher"

Scenario: UniKey researchers can be set as PI
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "principalInvestigator" with "unikey" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should see "CREATED"
	
Scenario: Non UniKey researchers cannot be set as PI
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "principalInvestigator" with "external" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should not see "CREATED"
	And I should see "The Principal Investigator is not a UniKey user, or they have not yet logged in into the Research Data Manager"
	
Scenario: UniKey researchers can be set as DMC
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "principalInvestigator" with "unikey" 
	And I fill in "dataManagementContact" with "unikey" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should see "CREATED"
	
Scenario: Non UniKey researchers cannot be set as DMC
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "principalInvestigator" with "unikey" 
	And I fill in "dataManagementContact" with "external" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should not see "CREATED"
	And I should see "The Data Management Contact is not a UniKey user, or they have not yet logged in into the Research Data Manager"
	
Scenario: UniKey is Mandatory
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should not see "CREATED"
	And I should see "Principal Investigator is a required field"
	
Scenario: Approved requests give full access to Principal Investigator
	Given I log in as "administrator"
	And I have an unapproved RDS request called "Unapproved" with PI "unikey"
	And there is a directory called "rg1"
	When I am on the Unapproved RDS requests page
	And I follow "Unapproved"
	And I fill in "dirPath" with "rg1"
	And I press "approve_button"
	Then I should see "APPROVED"
	And user "unikey" should have full access to group "Unapproved"
	