@passing @dc2d @dc2f
Feature: Autocomplete for subject code

Background: 
	Given I have the usual users and roles
	And I have the research subject code with code "0123456" and name "Test Code 1"
	And I have the research subject code with code "9876543" and name "Unreal Code"
	And I have the research subject code with code "6212694" and name "Unrippened code"
	And I have the research subject code with code "5555555" and name "Everything Five"
	Given I have a research group called "Research Group Test"
	And user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	
Scenario: Searching by subject code works
	Given I am on the request rds page	
	When I fill in "subjectCode" with "0123"
	Then I should see "0123456 - Test Code 1"

Scenario: Searching by subject name works
	Given I am on the request rds page	
	When I fill in "subjectCode" with "unr"
	Then I should see "9876543 - Unreal Code"
	Then I should see "6212694 - Unrippened code"
	Then I should not see "5555555 - Everything Five"
	Then I should not see "0123456 - Test Code 1"	

Scenario: Searching wrong input does not bring up any results
	Given I am on the request rds page	
	When I fill in "subjectCode" with "unc"
	Then I should not see "9876543 - Unreal Code"
	Then I should not see "6212694 - Unrippened code"
	Then I should not see "5555555 - Everything Five"
	Then I should not see "0123456 - Test Code 1"		
	
Scenario: Searching Principal Investigator on Research Group Form
	Given I am on the request rds page
	When I fill in "principalInvestigator" with "ictintersect"
	Then I should see "ictintersect2"
	Then I should see "ictintersect3"
	Then I should see "ictintersect4"
	Then I should see "ictintersect5"

Scenario: Searching Data Management Contact on Research Group Form
	Given I am on the request rds page
	When I fill in "principalInvestigator" with "ictintersect"
	Then I should see "ictintersect2"
	Then I should see "ictintersect3"
	Then I should see "ictintersect4"
	Then I should see "ictintersect5"

Scenario: Searching Unikey users on Edit PI form
	Given I log in as "ictintersect4"
	And I have an unapproved RDS request called "Unapproved" with PI "ictintersect5"	 
	When I am on the approve request page for research group "Unapproved"
	When I fill in "principalInvestigator" with "ictintersect"
	Then I should see "ictintersect2"
	Then I should see "ictintersect3"
	Then I should see "ictintersect4"
	Then I should see "ictintersect5"