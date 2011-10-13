@passing
Feature: Unikey User feature

Background:
	Given I have the usual users and roles
	Given I have a UniKey user "ICTIntersect1"
	And I log in as "ict_support"
	And I am on the assign role page


Scenario: Changing Unikey User roles
	Then I should see "ICTIntersect1"
	And I am on the edit roles page for "ICTIntersect1"
	Then I should see "Assign Role"
	And I select role "Administrator" from select "roles"
	And I press "submit"
	Then I should see role "Administrator"

Scenario: Adding additional roles to Unikey User
	Then I should see "ICTIntersect1"
	And I am on the edit roles page for "ICTIntersect1"
	Then I should see "Assign Role"
	And I add another role "Administrator" from select "roles"
	And I press "submit"
	Then I should see role "Administrator"
	And I should see role "Researcher"

Scenario: Setting no role to Unikey User
	Then I should see "ICTIntersect1"
	And I am on the edit roles page for "ICTIntersect1"
	Then I should see "Assign Role"
	And I select no roles from select "roles"
	And I press "submit"
	Then I should see role "None"
	