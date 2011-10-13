@passing
Feature: Autocomplete for subject code

Background: 
	Given I have the usual users and roles	
	And I have the research subject code with code "0123456" and name "Test Code 1"
	And I have the research subject code with code "9876543" and name "Unreal Code"
	And I have the research subject code with code "6212694" and name "Unrippened code"
	And I have the research subject code with code "5555555" and name "Everything Five"
	And I log in as "researcher"
	
Scenario: Searching by subject code works
	Given I am on the request rds page	
	When I fill in "subjectCode" with "0123"
	Then I sleep for "1" seconds
	Then I should see "0123456 - Test Code 1"

Scenario: Searching by subject name works
	Given I am on the request rds page	
	When I fill in "subjectCode" with "unr"
	Then I sleep for "1" seconds
	Then I should see "9876543 - Unreal Code"
	Then I should see "6212694 - Unrippened code"
	Then I should not see "5555555 - Everything Five"
	Then I should not see "0123456 - Test Code 1"	

Scenario: Searching wrong input does not bring up any results
	Given I am on the request rds page	
	When I fill in "subjectCode" with "unc"
	Then I sleep for "1" seconds
	Then I should not see "9876543 - Unreal Code"
	Then I should not see "6212694 - Unrippened code"
	Then I should not see "5555555 - Everything Five"
	Then I should not see "0123456 - Test Code 1"		
	
	 	
	 
	
