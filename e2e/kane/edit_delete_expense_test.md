---
mode: testing
target: emulator
app: ./app.apk
max_steps: 50
tags: [expenses]
---

# Edit and delete an expense

## Add an expense
Wait for the "Recurring Expenses" home screen to load. If the system asks for notification permission, allow it.
Tap the add button, type "Gym" into the "Name" field and "30" into the "Price" field, then tap the "Add Expense" button.
Verify the home screen lists an expense named "Gym" with the price 30.

## Change the price
Tap the "Gym" expense to open it.
Clear the "Price" field and type "35".
Scroll down in the form if needed and tap the "Save" button at the bottom of the form.
Verify the home screen lists "Gym" with the price 35.

## Delete the expense
Tap the "Gym" expense to open it again.
Tap the delete (trash can) icon at the top of the form, then confirm when asked "Do you really want to delete this expense?".
Verify the home screen no longer lists an expense named "Gym".
