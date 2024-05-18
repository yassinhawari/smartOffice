

function fetchDataFromFirebase() {
  // Initialize FirebaseApp
  const firebaseApp = FirebaseApp.getDatabaseByUrl(getEnvironment().firebaseUrl);

  // Get data from Firebase
  const data = firebaseApp.getData(getEnvironment().DATABASE_PATH);

  // Log the data (you can customize this part)
  Logger.log(data);

  // Update Google Sheet
  updateSheet(data);
}

  // Update Google Sheet
function updateSheet(data) {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const today = new Date();

  const formattedDate = Utilities.formatDate(today, "Africa/Tunis", "dd-MM-yyyy").toString();

  var lastRow = Math.floor(sheet.getLastRow()) + 1;

  // Merge the cells for the date
  sheet.getRange('A' + lastRow + ':A' + (lastRow + Object.keys(data).length - 1)).mergeVertically().setValue(formattedDate).setVerticalAlignment("middle").setHorizontalAlignment("Center").setBorder(false, false, true, false, false, false, "black", SpreadsheetApp.BorderStyle.SOLID);;

  
  // Iterate over each worker and append a new row with the current date and worker information
  for (var i = 0; i < Object.keys(data).length; i++) {
    var workerKey = 'worker' + (i + 1);
    var workerData = data[workerKey];
    var totalHour="0.0";
    var timeInMinutes = workerData.timeIn;
    var timeOutMinutes = workerData.timeOut;

    var totalMinutes = timeOutMinutes - timeInMinutes;
    var totalHours = Math.floor(totalMinutes / 60);
    var remainingMinutes = totalMinutes % 60;

var time=calculateTimeDifference(timeInMinutes,timeOutMinutes)
// Create a time format string (HH:mm)
    var totalHoursFormatted = Utilities.formatString('%02d:%02d', totalHours, remainingMinutes);
    if(totalHoursFormatted=="NaN:NaN"){
      totalHour="0.0";
    }
    else{
      totalHour=totalHoursFormatted;
    }
  
    var newRowData = [
      workerKey,
      workerData.name,
      workerData.group,
      workerData.email,
      workerData.timeIn,
      workerData.timeOut,
      time.toFixed(2) // Formula for total working hours
    ];

    // Set the values for the new row
    sheet.getRange(lastRow + i, 2, 1, 7).setValues([newRowData]).setBorder(false, false, false, true, false, false, "black", SpreadsheetApp.BorderStyle.SOLID);;
  }
  var range = sheet.getRange(lastRow+i-1, 1, 1, 8);
  range.setBorder(false, false, true, true, false, false, "black", SpreadsheetApp.BorderStyle.SOLID);
}
function calculateTimeDifference(time1, time2) {
  try {
    // Parse time strings into Date objects
    var date1 = parseTime(time1);
    var date2 = parseTime(time2);

    // Calculate the difference in milliseconds
    var differenceMs = date2 - date1;

    // Convert milliseconds to minutes
    var differenceMinutes = differenceMs / (1000 * 60); // 1 minute = 60 seconds = 60,000 milliseconds

    return differenceMinutes; // Return the difference in minutes
  } catch (error) {
    console.error("Error calculating time difference:", error);
    return 0; // Return 0 or handle the error gracefully
  }
}
function parseTime(timeStr) {
  // Parse time string in format "hh:mm:ss" into hours, minutes, and seconds
  var timeRegex = /^(\d{2}):(\d{2}):(\d{2})$/;
  var match = timeStr.match(timeRegex);

  if (!match) {
    throw new Error("Invalid time format");
  }

  var hours = parseInt(match[1], 10);
  var minutes = parseInt(match[2], 10);
  var seconds = parseInt(match[3], 10);

  // Create Date object with a common date and parsed time components
  var date = new Date(2000, 0, 1, hours, minutes, seconds);

  if (isNaN(date.getTime())) {
    throw new Error("Invalid time components");
  }

  return date;
}
function timeStringToMinutes(timeString) {
  var timeArray = timeString.split(":");
  var hours = parseInt(timeArray[0], 10);
  var minutes = parseInt(timeArray[1], 10);
  return hours * 60 + minutes;
}

function deleteTimeValuesFromFirebase() {
 const firebaseApp = FirebaseApp.getDatabaseByUrl(getEnvironment().firebaseUrl);

  // Get data from Firebase
  const data = firebaseApp.getData(getEnvironment().DATABASE_PATH);


  // Iterate over each worker and delete timeIn and timeOut values
  for (var workerKey in data) {
    var workerPath = getEnvironment().DATABASE_PATH + workerKey;
    
    // Update worker data with empty timeIn and timeOut values
    firebaseApp.updateData(workerPath, {
      timeIn: "",
      timeOut: ""
    });
  }
}



