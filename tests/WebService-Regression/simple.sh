CMD="curl -c cookie.txt -b cookie.txt -k -X POST https://pc-dev.com"

# echo -e "\n\n*** Sign-up ***"
# $CMD/json/user/account/signup/ -d '{ "email": "sl@pushcoin.com", "password": "test", "first_name": "Slaw", "last_name": "L.", "mobile_phone": "321-213-3123" }'
echo -e "\n\n*** Login ***"
$CMD/json/account/login/ -d '{ "user": "sl@pushcoin.com", "password": "test" }'

echo -e "\n\n*** Change Password ***"
$CMD/json/account/change-password/ -d '{ "old_password": "test", "new_password": "test2" }'
echo -e "\n\n*** Restore Password ***"
$CMD/json/account/change-password/ -d '{ "old_password": "test2", "new_password": "test" }'

echo -e "\n\n*** Logout ***"
$CMD/json/account/logout/ -d ''

echo -e "\n\n*** DONE ***"
