@wip @dc2d @dc2f
Feature: Login redirects to browse rds page

Background: 
	Given I have the usual users and roles

Scenario: User gets redirected to research group browse page
	Given I log in as "ictintersect2"
	Then I should be on the browse rds page