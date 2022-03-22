echo "-------------- Running the Swagger to restel's excel converter -------------------"
RESTEL_APP_FILE=$1
OUTPUT_FILE=$2
./gradlew runSwagger --args="$RESTEL_APP_FILE $OUTPUT_FILE"
echo "-------------- Done Swagger to restel's excel conversion -------------------"
