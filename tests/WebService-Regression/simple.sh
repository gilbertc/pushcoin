CMD="curl -c cookie.txt -b cookie.txt -k -X POST https://pc-dev.com"

# echo -e "\n\n*** Sign-up ***"
$CMD/json/user/account/signup/ -d '{ "email": "sl@pushcoin.com", "password": "test", "first_name": "Slaw", "last_name": "L.", "mobile_phone": "321-213-3123" }'
# $CMD/json/merchant/account/signup/ -d '{ "email": "sl@mc-231231231", "website": "http://www.mcdonalds.com/", "street": "1 Way St.", "city": "Chicago", "state": "IL", "zip": "50555", "business_name": "McDonalds", "phone": "321-213-3123" }'

echo -e "\n\n*** Login ***"
$CMD/json/account/login/ -d '{ "user": "sl@pushcoin.com", "password": "test" }'

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

echo -e "\n***************"
echo -e "\n*** Device ***"
echo -e "\n***************"

echo -e "\n\n*** Provision ***"
# $CMD/json/device/add-smart/ -d '{"description": "Jay iphone" }'
# $CMD/json/device/provision/ -d '{"serial_number": "123abc" }'
# $CMD/json/device/claim/ -d '{"registration_id": "P5MCRN7VHHVRZMXX", "description": "Jay wristband", "passcode": "1111" }'
# $CMD/json/device/reset-pin/ -d '{ "membership_id": "86a7f7cde789f3efa89db2ed887df61324824240", "passcode": "2222"}'
# $CMD/json/device/reset-description/ -d '{ "membership_id": "86a7f7cde789f3efa89db2ed887df61324824240", "description": "Kate keyfob"}'
$CMD/json/device/list/ -d ''

echo -e "\n***************"
echo -e "\n*** Transaction ***"
echo -e "\n***************"

$CMD/json/transaction/history/ -d '{ "page_num":0, "page_size":10}'

echo -e "\n\n*** Logout ***"
$CMD/json/account/logout/ -d ''

echo -e "\n\n*** DONE ***"
