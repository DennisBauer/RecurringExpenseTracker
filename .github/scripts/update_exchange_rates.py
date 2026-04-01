import os
import json
from datetime import datetime

api_key = os.getenv('CURRENCY_EXCHANGE_API_KEY')
files_dir = os.path.join(os.path.dirname(__file__), "./../../shared/src/commonMain/composeResources/files")

# --- Update exchange rates ---
exchange_rates_file = os.path.join(files_dir, "exchange_rates.json")
command = f'curl "https://api.currencyapi.com/v3/latest" -H "apikey: {api_key}"'

os.system(f'{command} > {exchange_rates_file}')

with open(exchange_rates_file, 'r') as file:
    data = json.load(file)

# Verify that the JSON contains the 'data' block
if 'data' not in data:
    raise ValueError("Downloaded exchange rates JSON does not contain the 'data' block.")

current_timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
data['updateTime'] = current_timestamp

with open(exchange_rates_file, 'w') as file:
    json.dump(data, file, indent=4)

# --- Update currencies ---
currencies_file = os.path.join(files_dir, "currencies.json")
currencies_command = f'curl "https://api.currencyapi.com/v3/currencies" -H "apikey: {api_key}"'

os.system(f'{currencies_command} > {currencies_file}')

with open(currencies_file, 'r') as file:
    currencies_data = json.load(file)

# Verify that the JSON contains the 'data' block
if 'data' not in currencies_data:
    raise ValueError("Downloaded currencies JSON does not contain the 'data' block.")

with open(currencies_file, 'w') as file:
    json.dump(currencies_data, file, indent=4)

