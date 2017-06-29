app.controller('authController', function($scope, $http) {
	$scope.headingTitle = 'Authentication';
	
	$scope.stepOneSuccessful = false;
	$scope.stepTwoSuccessful = false;
	$scope.sessionID = null;
	
	$scope.info = '';
	
	
	var d = new Date();
	$scope.bc = d.getTime();
	console.log($scope.bc);

	$scope.authenticateStepOne = function() {
		
		var userData = {
				username : $scope.user.name,
				password: $scope.user.password,
				pin : $scope.user.pin,
				businessKey : $scope.bc
		};
		
		var result = $http.post('login-username/', userData).then(function(result) {
			
			$scope.info = result.data.message;
			
			if(result.data.statusCode == 'SUCCESS') {
				$scope.stepOneSuccessful = true;
				$scope.sessionID = result.data.session;
			}
		});
	}
	
	$scope.authenticateStepTwo = function() {
		
		var userData = {
				username : $scope.user.name,
				password: $scope.user.password,
				pin : $scope.user.pin,
				session : $scope.sessionID,
				businessKey : $scope.bc
		};
		
		var result = $http.post('login-pin/', userData).then(function(result) {
			
			$scope.info = result.data.message;
			
			if(result.data.statusCode == 'SUCCESS') {
				$scope.stepTwoSuccessful = true;
				$scope.info = 'PIN was correct - Welcome!';
			} else {
				$scope.stepOneSuccessful = false;
				$scope.stepOneSuccessful = false;
			}
		});
	}
	
	$scope.logout = function() {
		
		var userData = {
				username : $scope.user.name,
				password: $scope.user.password,
				pin : $scope.user.pin,
				session : $scope.sessionID,
				businessKey : $scope.bc
		};
		
		var result = $http.post('logout/', userData).then(function(result) {
			
			$scope.info = result.data.message;
			
			if(result.data.statusCode == 'SUCCESS') {
				$scope.stepOneSuccessful = false;
				$scope.stepTwoSuccessful = false;
				$scope.sessionID = null;
			}
		});
	}
});

app.controller('aboutController', function($scope) {
	$scope.headingTitle = "Roles List";
});
