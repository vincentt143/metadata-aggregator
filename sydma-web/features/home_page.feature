@passing
Feature: Login redirects to correct page depending on user roles

Background: 
	Given I have the usual users and roles

Scenario: Researcher gets redirected to research group browse page
	Given I log in as "researcher"
	Then I should be on the browse rds page
	
Scenario: Support gets redirected to research group browse page
	Given I log in as "ict_support"
	Then I should be on the browse rds page
	
Scenario: Administrator gets redirected to admin home page
	Given I log in as "administrator"
	Then I should be on the administrator home page