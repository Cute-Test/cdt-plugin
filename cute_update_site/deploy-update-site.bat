set version=4.3.0

cd e:\cute\cute-checkout-workspace\cute_update_site
e:
tar -czvf update-site-%version%.tar.gz * --exclude=CVS --exclude=.project --exclude=update-site-* --exclude=deploy-update-site.bat

scp -i c:\Users\tcorbat\.ssh\id_rsa update-site-%version%.tar.gz tcorbat@www.cute-test.com:update-site-backup

ssh -i c:\Users\tcorbat\.ssh\id_rsa -t tcorbat@www.cute-test.com ^
sudo rm -r /var/www/redmine/updatesite/indigo/cute/* ; ^
sudo tar -xzvf /home/tcorbat/update-site-backup/update-site-%version%.tar.gz -C /var/www/redmine/updatesite/indigo/ ; ^
sudo chown root:root -R /var/www/redmine/updatesite/indigo/ ; ^
sudo chmod 644 -R /var/www/redmine/updatesite/indigo/ ; ^
sudo chmod 755 /var/www/redmine/updatesite/indigo /var/www/redmine/updatesite/indigo/cute /var/www/redmine/updatesite/indigo/cute/features /var/www/redmine/updatesite/indigo/cute/plugins

rm update-site-%version%.tar.gz