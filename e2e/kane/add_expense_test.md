---
mode: testing
target: emulator
app: ./app.apk
max_steps: 40
tags: [smoke, expenses]
---

# Add a monthly expense

## Open the add expense form
Wait for the "Recurring Expenses" home screen to load. If the system asks for notification permission, allow it.
Tap the "Add Expense" button (the + button) on the home screen.
Verify a form is shown with a "Name" field and a "Price" field.

## Fill in and save the expense
Type "Netflix" into the "Name" field and "15.99" into the "Price" field. Leave the recurrence at its default of every 1 month.
Scroll down in the form if needed and tap the "Add Expense" button at the bottom of the form.
Verify the home screen lists an expense named "Netflix" with the price 15.99.

## Check the monthly summary
Verify the "Monthly" summary at the top of the home screen shows 15.99.
