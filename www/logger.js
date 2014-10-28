module.exports = {
  getLogs: function(searchTerm, numLogs, success, failure) {
    cordova.exec(success, failure, "Logger", "getLogs", [searchTerm, numLogs]);
  }
};
