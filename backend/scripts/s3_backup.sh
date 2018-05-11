#! /bin/bash
# run after pg_backup.sh

# directory to save backups in, must be rwx by postgres user
BASE_DIR='/var/backups/postgres'
YMD=$(date "+%Y-%m-%d")
DIR="$BASE_DIR/$YMD"

S3_PATH="s3://ideadraw-backups/ideadraw_db_$YMD.out.gz"

# upload to s3
aws s3 cp "$DIR/db.out.gz" $S3_PATH
