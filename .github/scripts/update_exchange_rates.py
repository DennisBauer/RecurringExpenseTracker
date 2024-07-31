import os
import json
from datetime import datetime

api_key = os.getenv('CURRENCY_EXCHANGE_API_KEY')
command = f'curl "https://api.freecurrencyapi.com/v1/latest" -H "apikey: {api_key}"'
output_file = os.path.join(os.path.dirname(__file__), "./../../app/src/commonMain/composeResources/files/exchange_rates.json")

os.system(f'{command} > {output_file}')

with open(output_file, 'r') as file:
    data = json.load(file)

# Verify that the JSON contains the 'data' block
if 'data' not in data:
    raise ValueError("Downloaded JSON does not contain the 'data' block.")

current_timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
data['updateTime'] = current_timestamp

with open(output_file, 'w') as file:
    json.dump(data, file, indent=4)
