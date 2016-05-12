lecture = function(appName, appHomePath, resourceURL) {
    'use strict';
    var project = angular.module(appName, ['ngRoute', 'ngSanitize', 'ui.bootstrap', 'ngLoadingSpinner','mgcrea.ngStrap']);

    //config
    project.config(['$routeProvider', function($routeProvider) {
        $routeProvider.
                when('/', {
            controller: 'homeCtrl',
            templateUrl: appHomePath + '/views/home.html'
        }).
                otherwise(
                {redirectTo: '/'}
        );
    }]);

    //Home Controller
    project.controller('homeCtrl', function($scope, $http) {
        var treeVisibleState;

        //get context as JSON
        $http({method: 'GET', url: url(encodeUrl(resourceURL), "getJSON")}).
                success(function(data) {
            //i18n messages
            $scope.msgs = data.messages;
            //items display modes
            $scope.modes = [
                {id: 'all', value: $scope.msgs['all']},
                {id: 'notRead', value: $scope.msgs['notRead']},
                {id: 'unreadFirst', value: $scope.msgs['unreadFirst']}];
            //items not read displayed by default
            $scope.selectedMode = 'notRead';
            //categories
            $scope.cats = data.context.categories;
            //context name
            $scope.contextName = data.context.name;
            //first category selected by default
	      // $scope.selectedCats = [$scope.cats[0]];
	//Adaptation GIP RECIA 
	//Modification des categories selectionnées pour affichage par defaut 
		$scope.selectedCats = $scope.cats;

	// Adaptation GIP RECIA 
	// Recuperation de l'etat de l'authentificattion dans le scope 
	 	$scope.guestMode = data.guestMode;
            //all sources selected by default
            angular.forEach($scope.cats, function(cat, key) {
                cat.selectedSrcs = cat.sources;
            });
		

		  $scope.gererPlusieursCat = false;
          if($scope.cats.length > 1){
                        $scope.gererPlusieursCat = true;
                }
            $scope.allcats = new Array();
            angular.forEach($scope.cats, function(cat, key) {
                cat.selectedSrcs = cat.sources;
                var valIdCat = cat.id;
                cat.idHtml = valIdCat.split(":").join("");
                angular.forEach(cat.sources, function(src, key){
                        var val = src.id;
                        src.idHtml  =  val.split(":").join("");
                //      src.offsetY = $scope.elmYPosition(src.idHtml);
                //      $scope.allOffSets[src.offsetY]=src;
                          var colorSrc = src.color;
                          var nameSrc = src.name;
                          var hlSrc = src.highlight;
                          var rubId = src.idHtml;
                            angular.forEach(src.items, function (item,key){
                                item.rubriques = new Array();
                                var rubrique = new Object()
                                rubrique.color = colorSrc;
                                rubrique.name = nameSrc;
                                rubrique.idHtml = rubId;
                                item.rubriques.push(rubrique);
                                
                                           });

                        src.selectDesc = src.name + "<span ng-class=\"couleurRubrique("+src.color+")\" class=\"badge\" style=\"background-color:"+src.color+"\" >"+src.unreadItemsNumber+"</span>";
                        $scope.allcats.push(src);
                });
            });
		if($scope.gererPlusieursCat){
                	$scope.mySource = $scope.allcats[0];
		}else{
			$scope.mysource = $scope.cats[0].sources[0]
		}

            //tree visible state
            treeVisibleState = data.context.treeVisibleState;
        });

        //select a category and eventually a source to restrict displayed content
        $scope.select = function(catID, srcID) {
            angular.forEach($scope.cats, function(cat, key) {
                if (cat.id === catID) {
                    cat.isSelected = true;
                    $scope.selectedCats = [cat];
                    if (srcID) {
                        angular.forEach(cat.sources, function(src, key) {
                            if (src.id === srcID) {
                                src.isSelected = true;
                                cat.selectedSrcs = [src];
                            }else {
				// Patch pour deselectionner une source : utile pour ne pas afficher les sources vides dans la liste des actualités
				// corrige l'etat de la selection
				src.isSelected = false; 	
			}	
                        });
                    }
                    else {
                        cat.selectedSrcs = cat.sources;
			// Patch pour deselectionner toutes les sources selectionnées corrige l'etat de la selection 
			 angular.forEach(cat.sources, function(src, key) {
				src.isSelected = false ;
			});
                    }
                }
                else {
                    cat.isSelected = false;
                }
            });
        };

        // fold | unfold a category
        $scope.toggle = function(catID) {
            angular.forEach($scope.cats, function(cat, key) {
                if (cat.id === catID) {
                    cat.folded = !cat.folded;
                }
            });
        };
	//@params : item l'objet item 
	//@return boolean
	 $scope.isItemEmpty=function(item) {	
		// test si l'item est vide (un peu laborieux mais on a pas de standart : c'est different selon la source )
		if (item === null || item === undefined 
		|| (item.htmlContent === null && item.mobileHtmlContent === null )
		|| (item.htmlContent ==undefined && item.mobileHtmlContent === undefined) 
		|| (item.htmlContent === '\n' && item.mobileHtmlContent === '\n')
		|| (item.htmlContent === '' && item.mobileHtmlContent === '')){
		 	return true ;
		}
		return false; 
        };
	// test si la source(regroupement d'items) est vide
	// @params : idSrc identifiant de la source
	//           idCat idebtifiant de la categorie
	//@return : boolean
	 $scope.isSourceEmpty = function(idSrc,idCat){
                var testResult = true;
                angular.forEach($scope.cats, function(cat, key){
                        if(cat.id == idCat){
				
                                angular.forEach(cat.sources, function(source,key){
                                        if (source.id === idSrc){
                                                angular.forEach(source.items, function (item,key){
						
                                                        if (!$scope.isItemEmpty(item)){
                                                                testResult = false;
                                                        }
                                                });     
                                        }
					 	
					
                                });
                         }
                });
                 
                 return testResult;
        };
	// test si la categorie (regrouopement de sources ) est vide 
	// @param : idCat identifiant de la categorie a tester
	// @return : boolean 
	$scope.isCatEmpty = function(idCat){
                var testResult = true;
		angular.forEach($scope.cats, function(cat, key){
			if (cat.id === idCat ) {
				angular.forEach(cat.sources, function(source,key){
				if (!$scope.isSourceEmpty(source.id, idCat)) {
					
				 	testResult= false;
				}
		
				});
			} 
		});	
		
	 	 return testResult;
		
	};
	
	$scope.isSelectedCat=function(cat){
	
		return cat.isSelected ;
	};
	$scope.isSelectedSrc = function (src){
		return src.isSelected ; 	
	}
        // mark as read or unread on source item
        $scope.toggleItemReadState = function(cat, src, item) {
            //call server to store information
            $http({method: 'GET', url: url(encodeUrl(resourceURL), "toggleItemReadState", cat.id, src.id, item.id, !item.read)}).
                    success(function(data) {
                (item.read ? src.unreadItemsNumber++ : src.unreadItemsNumber--);
                item.read = !item.read;
            });
        };

        // mark as read or unread all displayed source items
        $scope.markAllItemsAsRead = function(flag) {
            angular.forEach($scope.selectedCats, function(cat, key) {
                angular.forEach(cat.selectedSrcs, function(src, key) {
                    angular.forEach(src.items, function(item, key) {
                        if (item.read !== flag) {
                            $scope.toggleItemReadState(cat, src, item);
                        }
                    });
                });
            });
        };

        // evaluate is tree should be displayed
        $scope.treeDisplayed = function() {
            if (treeVisibleState === "NOTVISIBLE")
                return false;
            if (treeVisibleState === "NEVERVISIBLE")
                return false;
            if (treeVisibleState === "VISIBLE")
                return true;
        };

        // show the tree
        $scope.showTree = function() {
            treeVisibleState = "VISIBLE";
        };

        // hide the tree
        $scope.hideTree = function() {
            treeVisibleState = "NOTVISIBLE";
        };

        $scope.callTooltip = function(obj) {
            $( '#'+obj).fadeToggle('slow');
        }

        $scope.status = {
            isopen: false
        };

        $scope.toggleDropdown = function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.status.isopen = !$scope.status.isopen;
        };

    });
    
    //Mode Filter
    project.filter('modeFilter', function() {
        var modeFilter = function(input, selectedMode) {
            var ret = new Array();
            var i = 0;
            angular.forEach(input, function(item, key) {
                ret.push(item);
                i++;
                if (item.read && (selectedMode === "notRead" || selectedMode === "unreadFirst")) {
                    ret.splice(i - 1, 1);
                    i--;
                }
                ;
            });
            angular.forEach(input, function(item, key) {
                if (item.read && selectedMode === "unreadFirst") {
                    ret.push(item);
                }
                ;
            });
            return ret;
        };
        return modeFilter;
    });
    

};

lectureEdit = function(appName, appHomePath, resourceURL) {
    var project = angular.module(appName, ['ngRoute', 'ngSanitize']);

    //config
    project.config(['$routeProvider', function($routeProvider) {
        $routeProvider.
                when('/', {
            controller: 'editCtrl',
            templateUrl: appHomePath + '/views/edit.html'
        }).
                otherwise(
                {redirectTo: '/'}
        );
    }]);

    project.controller('editCtrl', function ($scope, $http) {

        refreshJSON();
        
        //get context as JSON
        function refreshJSON() {
            $http({method: 'GET', url: url(encodeUrl(resourceURL), "getEditJSON")}).
                    success(function(data) {
                //i18n messages
                $scope.msgs = data.messages;
                //context
                $scope.ctx = data.context;
                //categories
                $scope.cats = data.context.categories;
                //context name
                $scope.contextName = data.context.name;
                //first category selected by default
                $scope.selectedCat = $scope.cats[0];
            });        
        }
        
        $scope.select = function(cat) {
            $scope.selectedCat = cat;
        };
        
        $scope.toogleCategorySubcribtion = function(cat) {
            $http({method: 'GET', url: url(encodeUrl(resourceURL), "toogleCategorySubcribtion", $scope.ctx.id, cat.id, cat.subscribed)}).
                    success(function(data) {
                cat.subscribed = !cat.subscribed;
                cat.notSubscribed = !cat.notSubscribed;
                refreshJSON();
            });
        };

        $scope.toogleSourceSubcribtion = function(cat, src) {
            $http({method: 'GET', url: url(encodeUrl(resourceURL), "toogleSourceSubcribtion", cat.id, src.id, src.subscribed)}).
                    success(function(data) {
                src.subscribed = !src.subscribed;
                src.notSubscribed = !src.notSubscribed;
            });            
        };
    });
        
};

// ************* utils *************

function encodeUrl(requestUrl) {
    return requestUrl.
        replace(/%40/gi, '@').
        replace(/%3A/gi, ':').
        replace(/%24/g, '$').
        replace(/%2C/gi, ',');
}

//forge a portlet resource url
function url(pattern, id, p1, p2, p3, p4) {
    return pattern.
            replace("@@id@@", id).
            replace("__p1__", p1).
            replace("__p2__", p2).
            replace("__p3__", p3).
            replace("__p4__", p4);
}
