Feature: External User feature

Background:
	Given I have the usual users and roles
	And I log in as "administrator"
	And I am on the user management page

Scenario: Creating a new external user
	Given I am on the create external user page
	And I create external user details for user "User"
	Then I am on the view external users page
	Then I should see "External User List"
	And I should see "user@integration.test"

Scenario: Editing an existing external user
	Given I create external user details for user "Old User"
	Given I am on the edit external user page for user "Old User"
	And I fill in "email" with "change@integration.test"
	And I fill in "password" with "change@integration.test"
	And I fill in "confirm_password" with "change@integration.test"
	And I press "submit"
	Then I should see "External User List"
	And I should see "change@integration.test"
	
Scenario: Logging in using new external user
	Given I create external user details for user "User"
	Given I am on the login page
	Then I log in as "user@integration.test"
	Then I should see "My Research Data Manager"

Scenario: Logging in using edited external user
	Given I create external user details for user "Old User"
	Given I am on the edit external user page for user "Old User"
	And I fill in "email" with "change@integration.test"
	And I fill in "password" with "change@integration.test"
	And I fill in "confirm_password" with "change@integration.test"
	And I press "submit"
	Then I am on the login page
	And I log in as "change@integration.test"
	Then I should see "My Research Data Manager"
	
Scenario: Password has to be greater than 6 characters
	Given I am on the create external user page
	And I fill in "password" with "abcde"
	And I press "submit"
	Then I should see "Password must be greater than 6 characters"
	
Scenario: Username is the same as email
	Given I am on the create external user page	
	And I fill in "email" with "test@email.com"
	And I fill in "password" with "test@email.com"
	And I fill in "confirm_password" with "test@email.com"
	And I fill in "givenname" with "Given Name"
	And I fill in "surname" with "Surname"
	And I press "submit"
	Then the username should be the same as email "test@email.com"
	
	