cd e:\cute\cute-checkout-workspace\cute_update_site
e:
tar -czvf update-site-4.2.4.tar.gz * --exclude=CVS --exclude=.project

scp -i c:\Users\tcorbat\.ssh\id_rsa update-site-4.2.4.tar.gz tcorbat@www.cute-test.com:update-site-backup

ssh -i c:\Users\tcorbat\.ssh\id_rsa -t tcorbat@www.cute-test.com ^
sudo rm -r /var/www/redmine/updatesite/cute/* ; ^
sudo tar -xzvf /home/tcorbat/update-site-backup/update-site-4.2.4.tar.gz -C /var/www/redmine/updatesite/ ; ^
sudo chown root:root -R /var/www/redmine/updatesite/ ; ^
sudo chmod 644 -R /var/www/redmine/updatesite/ ; ^
sudo chmod 755 /var/www/redmine/updatesite/ /var/www/redmine/updatesite/cute /var/www/redmine/updatesite/cute/features /var/www/redmine/updatesite/cute/plugins

rm update-site-4.2.4.tar.gz