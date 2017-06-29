var app = angular.module('app', ['ngRoute','ngResource']);
app.config(function($routeProvider){
    $routeProvider
        .when('/auth',{
            templateUrl: '/views/auth.html',
            controller: 'authController'
        })
        .when('/roles',{
            templateUrl: '/views/about.html',
            controller: 'aboutController'
        })
        .otherwise(
            { redirectTo: '/'}
        );
});

