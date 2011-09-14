cd e:\cute\cute-checkout-workspace\cute_update_site_helios
e:
tar -czvf update-site-helios-4.1.2.tar.gz * --exclude=CVS --exclude= BuildUpdateSite.bat --exclude=.project --exclude=update-site-* --exclude=deploy-update-site.bat

scp -i c:\Users\tcorbat\.ssh\id_rsa update-site-helios-4.1.2.tar.gz tcorbat@www.cute-test.com:update-site-backup

ssh -i c:\Users\tcorbat\.ssh\id_rsa -t tcorbat@www.cute-test.com ^
sudo rm -r /var/www/redmine/updatesite/helios/cute/* ; ^
sudo tar -xzvf /home/tcorbat/update-site-backup/update-site-helios-4.1.2.tar.gz -C /var/www/redmine/updatesite/helios/ ; ^
sudo chown root:root -R /var/www/redmine/updatesite/helios ; ^
sudo chmod 644 -R /var/www/redmine/updatesite/helios ; ^
sudo chmod 755 /var/www/redmine/updatesite/helios /var/www/redmine/updatesite/helios/cute /var/www/redmine/updatesite/helios/cute/features /var/www/redmine/updatesite/helios/cute/plugins

rm update-site-helios-4.1.2.tar.gz