CMD="curl -c cookie.txt -b cookie.txt -k -X POST https://pc-dev.com"

# echo -e "\n\n*** Sign-up ***"
$CMD/json/account/initiate-signup/ -d '{ "email": "sl+test1@pushcoin.com" }'
$CMD/json/account/validate-registration-code/ -d '{ "account_id": "RYRKAYC7AP" }'
$CMD/json/user/account/complete-signup/ -d '{ "account_id": "LFY7KYFATP", "password": "Argon-55", "first_name": "Slaw", "last_name": "L.", "street": "712 Fergusson Rd", "city": "Menchester", "state": "WA", "zip": "70123" }'

echo -e "\n\n*** Login ***"
$CMD/json/account/login/ -d '{ "user": "sl@pushcoin.com", "password": "Argon-55" }'

echo -e "\n***************"
echo -e "\n*** Account ***"
echo -e "\n***************"

echo -e "\n\n*** Change Password ***"
$CMD/json/account/change-password/ -d '{ "old_password": "test", "new_password": "test2" }'
echo -e "\n\n*** Restore Password ***"
$CMD/json/account/change-password/ -d '{ "old_password": "test2", "new_password": "test" }'
echo -e "\n\n*** Change Name ***"
$CMD/json/account/change-person-name/ -d '{ "first_name": "first_test_long_and_ugly", "last_name": "last_test_long_and_ugly" }'
echo -e "\n\n*** Restore Name ***"
$CMD/json/account/change-person-name/ -d '{ "first_name": "first_test", "last_name": "last_test" }'
echo -e "\n\n*** Account Summary ***"
$CMD/json/account/summary/ -d ''
echo -e "\n\n*** Balance ***"
$CMD/json/account/balance/ -d ''
echo -e "\n\n*** PIN threshold ***"
$CMD/json/account/get-pin-threshold/ -d ''
$CMD/json/account/set-pin-threshold/ -d '{ "cumulative": 15.00}'
$CMD/json/account/get-pin-threshold/ -d ''
$CMD/json/account/set-pin-threshold/ -d '{}'
$CMD/json/account/get-pin-threshold/ -d ''

echo -e "\n***************"
echo -e "\n*** Recovery ***"
echo -e "\n***************"
$CMD/json/recovery/list-possible-password-reset-question/ -d '{ }'
$CMD/json/recovery/add-password-reset-challenge/ -d '{ "question_id": 3, "answer": "never" }'
$CMD/json/recovery/delete-password-reset-challenge/ -d '{ "question_id": 3 }'
$CMD/json/recovery/get-password-reset-challenge/ -d '{ "tracking_id": "fdas" }'
$CMD/json/recovery/list-password-reset-challenge/ -d '{ }'

echo -e "\n***************"
echo -e "\n*** Device ***"
echo -e "\n***************"

echo -e "\n\n*** Provision ***"
# $CMD/json/device/add-smart/ -d '{"description": "Jay iphone" }'
# $CMD/json/device/provision/ -d '{"serial_number": "123abc" }'
# $CMD/json/device/claim/ -d '{"registration_id": "EVJX2Q62F7GOMPAU", "description": "Jay wristband", "passcode": "1111" }'
# $CMD/json/device/reset-pin/ -d '{ "membership_id": "86a7f7cde789f3efa89db2ed887df61324824240", "passcode": "2222"}'
# $CMD/json/device/reset-description/ -d '{ "membership_id": "86a7f7cde789f3efa89db2ed887df61324824240", "description": "Kate keyfob"}'
$CMD/json/device/list/ -d ''

echo -e "\n***************"
echo -e "\n*** Transaction ***"
echo -e "\n***************"

$CMD/json/transaction/history/ -d '{ "page_num":0, "page_size":10}'

echo -e "\n***************"
echo -e "\n*** Bookmarks ***"
echo -e "\n***************"

$CMD/json/bookmark/recipient/list/ -d ''
$CMD/json/bookmark/recipient/add/ -d '{"email": "sl@minta.com", "description": "my first bookmark"}'
$CMD/json/bookmark/recipient/delete/ -d '{"email": "sl@minta.com"}'
 $CMD/json/bookmark/ach/add/ -d '{ "routing_number": "307087713", "bank_account_number": "1211231", "bank_account_name": "Samsung Electr.",  "description": "secondary account"}'
$CMD/json/bookmark/ach/list/ -d ''
$CMD/json/bookmark/address/list/ -d ''

echo -e "\n\n*** Logout ***"
$CMD/json/account/logout/ -d ''

echo -e "\n\n*** Whitelist ***"
$CMD/json/whitelist/list_membership/ -d ''
$CMD/json/whitelist/list_candidate/ -d ''
$CMD/json/whitelist/add/ -d '{"transaction_id":"4CCNHXHCKXFFRRXRYLNT"}'
$CMD/json/whitelist/delete/ -d '{"whitelist_id":"2"}'

echo -e "\n***************"
echo -e "\n*** Merchant ***"
echo -e "\n***************"
# $CMD/json/merchant/account/signup/ -d '{ "email": "sl@mcd.com", "password": "Argon-55", "first_name": "Slaw", "last_name": "L.", "gov_id": "12-231231231", "website": "http://www.mcdonalds.com/", "street": "1 Way St.", "city": "Chicago", "state": "IL", "zip": "50555", "business_name": "McDonalds", "phone": "321-213-3123" }'

$CMD/json/account/login/ -d '{ "user": "sl@mcd.com", "password": "Argon-55" }'
echo -e "\n\n*** Change Business info ***"
$CMD/json/merchant/account/change-info/ -d '{ "gov_id": "12-9999999", "website": "http://www.mcd.com/", "street": "10 Way St.", "city": "Calgary", "state": "AB", "zip": "T2Z 2P1", "business_name": "Wendys", "phone": "403-999-8888" }'

echo -e "\n\n*** DONE ***"
