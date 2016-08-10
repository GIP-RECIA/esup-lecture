lecture = function(appName, appHomePath, resourceURL) {
	'use strict';
	var project = angular.module(appName, [ 'ngRoute', 'ngSanitize',
	                                        'ui.bootstrap', 'ngLoadingSpinner', 'mgcrea.ngStrap' ]);

	// config
	project.config([ '$routeProvider', function($routeProvider) {
		$routeProvider.when('/', {
			controller : 'homeCtrl',
			templateUrl : appHomePath + '/views/home.html'
		}).otherwise({
			redirectTo : '/'
		});
	} ]);
	project.directive('a', function() {
		return {
			restrict : 'E',
			link : function(scope, elem, attrs) {
				if (attrs.ngClick || attrs.href === '' || attrs.href === '#') {
					elem.on('click', function(e) {
						e.preventDefault();
					});
				}
			}
		};
	});
	// Home Controller
	project.controller('homeCtrl',function($scope, $http) {
		var treeVisibleState;

		// get context as JSON
		$http({
			method : 'GET',
			url : url(encodeUrl(resourceURL), "getJSON")
		}).success(function(data) {
			// i18n messages
			$scope.msgs = data.messages;
			// items display modes
			$scope.modes = [
			                {
			                	id : 'all',
			                	value : $scope.msgs['all']
			                },
			                {
			                	id : 'notRead',
			                	value : $scope.msgs['notRead']
			                },
			                {
			                	id : 'unreadFirst',
			                	value : $scope.msgs['unreadFirst']
			                }, {
			                	id : 'readFirst',
			                	value : $scope.msgs['readFirst']
			                } ];
			
			
			
			
			// items not read displayed by
			// default
			// $scope.selectedMode = 'notRead';

			// All items showed by defaukt
			$scope.selectedMode = 'readFirst';
			// categories
			$scope.cats = data.context.categories;
			// context name
			$scope.contextName = data.context.name;
			// first category selected by
			// default
			// $scope.selectedCats =
			// [$scope.cats[0]];
			// Adaptation GIP RECIA
			// Modification des categories
			// selectionnées pour affichage par
			// defaut
			$scope.selectedCats = $scope.cats;

			// Adaptation GIP RECIA
			// Recuperation de l'etat de
			// l'authentificattion dans le scope
			$scope.guestMode = data.guestMode;
			// all sources selected by default
			angular
			.forEach(
					$scope.cats,
					function(cat, key) {
						cat.selectedSrcs = cat.sources;
					});
			$scope.gererPlusieursCat = false;
			if ($scope.cats.length > 1) {
				$scope.gererPlusieursCat = true;
			}
			$scope.allcats = new Array();
			var allUnread = 0; 
			var tableauNLTrieParDate = new Array();
			var tableauLuesTrieParDate = new Array();
			angular.forEach($scope.cats,function(cat, key) {
				cat.selectedSrcs = cat.sources;
				var valIdCat = cat.id;
				cat.idHtml = valIdCat.split(":").join("");
				angular.forEach(cat.sources,function(src,key) {
					var val = src.id;
					src.idHtml = val.split(":").join("").split(" ").join("");
					src.selectDesc = src.name
					+ "<span ng-class=\"couleurRubrique("
					+ src.color
					+ ")\" class=\"badge\" style=\"background-color:"
					+ src.color
					+ "\" >"
					+ src.unreadItemsNumber
					+ "</span>";
					$scope.allcats.push(src);
				});
				
				angular.forEach(cat.sources,function(source,key) {
					angular.forEach(source.items,function(item,key) {
							item.DatePubl = new Date(item.pubDate);
												if(!item.read){
													// On augmente le total des annonces non lues 
													allUnread ++;
													// On ajoute l'element dans le tableau des non lues 
													// Si il ne s(y trouve pas deja via une autre source(ou rubrique)
													if (!$scope.containsElement(tableauNLTrieParDate, item)){
														tableauNLTrieParDate.push(item);
													}
												}else{
													if (!$scope.containsElement(tableauLuesTrieParDate, item)){
														tableauLuesTrieParDate.push(item);
													}
												}
												
												
                                                angular.forEach(item.rubriques, function(rubrique, key){
                                                        var index, len;
                                                     // Creation des ancres pour les rubriques
                                                        for (index = 0, len = cat.sources.length; index<len; ++index){
                                                                if(rubrique.nom == cat.sources[index].name){
                                                                        rubrique.idHtml = cat.sources[index].idHtml;
                                                                }
                                                        }
                                                        
                                                });
                                        });
				});

			});
			$scope.mySource = $scope.allcats[0];
			
			$scope.isAccueil = data.context.viewDef; 
			if ($scope.isAccueil){
				 // Trie par date du tableau des non lues
				tableauNLTrieParDate.sort(function(a,b){
					if (a.DatePubl > b.DatePubl)
						return -1;
					if(a.DatePubl < b.DatePubl)
						return 1;
					return 0;
				
				});
				 // Trie par date du tableau des non lues
				tableauLuesTrieParDate.sort(function(a,b){
					if (a.DatePubl > b.DatePubl)
						return -1;
					if(a.DatePubl < b.DatePubl)
						return 1;
					return 0;
				
				});
				$scope.numberUnread = allUnread;
				$scope.lienVue = data.context.lienVue;
				// Si on a une rubrique a la une : 
				if($scope.cats[0].sources[0].name.toUpperCase() == "A LA UNE"){
					$scope.acceuilItems = $scope.cats[0].sources[0].items;
					var j = 0;
					var i =0 ;
					var len = $scope.acceuilItems.length;
					//Si on a pas assez d'elements dans la rubrique A LA UNE 
					if (data.context.nombreArticle > $scope.acceuilItems.length){
						// Tant que l'on a pas fini de remplir le nombre d'elements demandé : 
						while ($scope.acceuilItems.length < data.context.nombreArticle && j<= tableauNLTrieParDate.length ){
							if(!$scope.containsElement(acceuilItems, tableauNLTrieParDate[j])){
								// On complete par des non lues;
									$scope.acceuilItems[len+i] = tableauNLTrieParDate[j];
									i++;
								}
							j++;
						}
						// Si le tableau n'est tjrs pas complet : on complete par des lues les plus recentes : 
						if(data.context.nombreArticle >$scope.acceuilItems.length){
							i, j = 0;
							len = $scope.acceuilItems.length;
							while ($scope.acceuilItems.length < data.context.nombreArticle && j<= tableauLuesTrieParDate.length ){
								if(!$scope.containsElement(acceuilItems, tableauLuesTrieParDate[j])){
									// On complete par des lues;
										$scope.acceuilItems[len+i] = tableauLuesTrieParDate[j];
										i++;
									}
								j++;
							}
						}// Si le tableau n'est tjrs pas complet on ne peut plus rien faire

						
					}else if (data.context.nombreArticle < $scope.cats[0].sources[0].length) { // Si on a trop d'elements dans la rubrique "A LA UNE"
						for (var i = data.context.nombreArticle; i<$scope.cats[0].sources[0].length; i++){
							$scope.acceuilItems.splice(i,1);
						}
					}
				} else{ // Si on a pas de rubrique a la une 
					var i;
					 $scope.acceuilItems = new Array();
					if(data.context.nombreArticle >= tableauNLTrieParDate.length){
						// Si on a pas assez d'elements non lus 
						for (i=0; i<tableauNLTrieParDate.length; i++){
							// On prend ce qu'on a 
							$scope.acceuilItems.push(tableauNLTrieParDate[i]);
						}
						var restant = data.context.nombreArticle - tableauNLTrieParDate.length;
						var len = $scope.acceuilItems.length;
						if (restant <= tableauLuesTrieParDate.length){
							for (i=0; i<restant; i++){
								// On complete avec des elements lus
								$scope.acceuilItems[len - i] = tableauLuesTrieParDate[i];
							}
						}else {
							for (i=0; i<tableauLuesTrieParDate.length; i++){
								$scope.acceuilItems[len - i] = tableauLuesTrieParDate[i];
							}
						}
					}else{
						// Sinon
						// On prend les n les plus recents (car le tableau est deja trié)
						for (i=0; i<data.context.nombreArticle; i++){
							$scope.acceuilItems.push(tableauNLTrieParDate[i]);
						}
					}
					
				}
				
			}
			
			treeVisibleState = data.context.treeVisibleState;
		});

		$scope.isSelectFocused = false;
		$scope.sourceChanged = function(value) {
			$scope.doScrollTo(value);
		}
		$scope.selectFocused = function(value) {
			var body = angular.element(document
					.querySelector('body'));
			body.append("<div class=\"hideBody\"></div>");
			body.addClass("noScroll");

		}
		$scope.selectBlured = function(value) {
			var bodyHider = angular.element(document
					.querySelector('.hideBody'));
			bodyHider.removeClass('hideBody');
			var body = angular.element(document
					.querySelector('body'));
			body.removeClass("noScroll");
		}

		$scope.containsElement = function(theArray, element) {
				var i;
				for (i=0; i<theArray.length; i++){
					if (theArray[i].id == element.id){
						return true;
					}
				}
				return false;
		}

		var lastScrollTop = 0;
		angular.element($(window)).bind("scroll",function() {
			var isElementVisible = angular.element(
					document.querySelector("#mobileSelect")).is(':visible');
			if (isElementVisible == true) {
				// var st = $(this).scrollTop();
				var st = $scope.currentYPosition();
				$scope.allcats.forEach(function(src) {
					var pos = $scope.elmYPosition(src.idHtml);
					if (st > lastScrollTop
							&& pos > 0
							&& st >= pos) {
						$scope.mySource = src;
						$scope.$apply();
					} else if (st < lastScrollTop
							&& pos > 0
							&& src != $scope.mySource) {
						var trgSrc = $scope.getMinPos(st,src);
						if (trgSrc != undefined
								&& trgSrc != null) {
							$scope.mySource = trgSrc;
							$scope.$apply();
						}
					}

				});
				lastScrollTop = st;
			}
		});
		$scope.getMinPos = function(curPos, src) {
			var pos = $scope.elmYPosition(src.idHtml);
			var posMysource = $scope.elmYPosition($scope.mySource.idHtml);
			if (curPos <= pos && pos <= posMysource) {
				return src;
			}
			return null;
		}
		$scope.couleurRubrique = function(value) {
			return {
				"background-color" : value.color + "!important"
			};
		};

		// select a category and eventually a source to restrict
		// displayed content
		$scope.select = function(catID, srcID) {
			angular.forEach($scope.cats, function(cat, key) {
				if (cat.id === catID) {
					cat.isSelected = true;
					$scope.selectedCats = [ cat ];
					if (srcID) {
						angular.forEach(cat.sources, function(
								src, key) {
							if (src.id === srcID) {
								src.isSelected = true;
								cat.selectedSrcs = [ src ];
							} else {
								// Patch pour deselectionner une
								// source : utile pour ne pas
								// afficher les sources vides
								// dans la liste des actualités
								// corrige l'etat de la
								// selection
								src.isSelected = false;
							}
						});
					} else {
						cat.selectedSrcs = cat.sources;
						// Patch pour deselectionner toutes les
						// sources selectionnées corrige l'etat
						// de la selection
						angular.forEach(cat.sources, function(
								src, key) {
							src.isSelected = false;
						});
					}
				} else {
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
		// @params : item l'objet item
		// @return boolean
		$scope.isItemEmpty = function(item, color) {
			// test si l'item est vide (un peu laborieux mais on
			// a pas de standart : c'est different selon la
			// source )
			if ((item === null || item === undefined)
					|| (item.htmlContent === null && item.mobileHtmlContent === null)
					|| (item.htmlContent == undefined && item.mobileHtmlContent === undefined)
					|| (item.htmlContent === '\n' && item.mobileHtmlContent === '\n')
					|| (item.htmlContent === '' && item.mobileHtmlContent === '')
					|| (item.htmlContent === '<br>' && item.mobileHtmlContent === '<br>')) {
				return true;
			}
			// L'item n'est pas vide on lui assigne une source
			return false;
		};
		// test si la source(regroupement d'items) est vide
		// @params : idSrc identifiant de la source
		// idCat idebtifiant de la categorie
		// @return : boolean
		$scope.isSourceEmpty = function(idSrc) {
			var testResult = true;
			angular.forEach($scope.cats, function(cat, key) {
				angular.forEach(cat.sources, function(source,
						key) {
					if (source.id === idSrc) {
						var color = source.color;
						angular.forEach(source.items, function(
								item, key) {
							if (!$scope
									.isItemEmpty(item, color)) {
								testResult = false;
							}
						});
					}
				});
			});

			return testResult;
		};
		// test si la categorie (regrouopement de sources ) est
		// vide
		// @param : idCat identifiant de la categorie a tester
		// @return : boolean
		$scope.isCatEmpty = function(idCat) {
			var testResult = true;
			angular.forEach($scope.cats, function(cat, key) {
				if (cat.id === idCat) {
					angular.forEach(cat.sources, function(
							source, key) {
						if (!$scope.isSourceEmpty(source.id,
								idCat)) {

							testResult = false;
						}

					});
				}
			});

			return testResult;

		};

		$scope.isSelectedCat = function(cat) {

			return cat.isSelected;
		};
		$scope.isSelectedSrc = function(src) {
			return src.isSelected;
		}
		// mark as read or unread on source item
		$scope.toggleItemReadState = function(src, item) {
			var cat;
			angular.forEach($scope.cats,
					function(category, key) {
				angular.forEach(category.sources,
						function(source, key) {
					if (source.id === src.id) {
						cat = category;
					}
				});

			});

			// call server to store information
			$http(
					{
						method : 'GET',
						url : url(encodeUrl(resourceURL),
								"toggleItemReadState", cat.id,
								src.id, item.id, !item.read)
					})
					.success(
							function(data) {
								angular.forEach($scope.cats,function(category,key) {
									angular.forEach(category.sources,function(source,key) {
										angular.forEach(source.items,function(itm) {
											if (itm.id == item.id) {
												(itm.read ? source.unreadItemsNumber++
														: source.unreadItemsNumber--);
												itm.read = !itm.read;
											}

										});
									});

								});

							});
		};

		// mark as read or unread all displayed source items
		$scope.markAllItemsAsRead = function(flag) {
			angular.forEach($scope.selectedCats, function(cat,key) {
				angular.forEach(cat.selectedSrcs, function(src,key) {
					angular.forEach(src.items, function(item,key) {
						if (item.read !== flag) {
							$scope.toggleItemReadState(cat,src, item);
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
			$('#' + obj).fadeToggle('slow');
		}

		$scope.status = {
				isopen : false
		};

		$scope.toggleDropdown = function($event) {
			$event.preventDefault();
			$event.stopPropagation();
			$scope.status.isopen = !$scope.status.isopen;
		};
		// Realiser le scroll fonction appellée au click
		$scope.doScrollTo = function(eID) {
			// Position courrante

			var startY = $scope.currentYPosition();
			// Position de l'element cible
			var stopY = $scope.elmYPosition(eID);
			// Distance entre les elements
			var distance = stopY > startY ? stopY - startY
					: startY - stopY;
			// Si l'element est visible
			if (distance < 100) {
				scrollTo(0, stopY);
				return;
			}
			// Vitesse de scroll
			var speed = Math.round(distance / 100);
			// limitation de la vitesse
			if (speed >= 20)
				speed = 20;
			var step = Math.round(distance / 25);
			var leapY = stopY > startY ? startY + step : startY
					- step;
			var timer = 0;
			// Si scroll vers le bas
			if (stopY > startY) {
				for (var i = startY; i < stopY; i += step) {
					setTimeout("window.scrollTo(0, " + leapY
							+ ")", timer * speed);
					leapY += step;
					if (leapY > stopY)
						leapY = stopY;
					timer++;
				}
				return;
			}
			for (var i = startY; i > stopY; i -= step) {
				setTimeout("window.scrollTo(0, " + leapY + ")",
						timer * speed);
				leapY -= step;
				if (leapY < stopY)
					leapY = stopY;
				timer++;
			}

			/*
			 * function currentYPosition() { if
			 * (self.pageYOffset) { return self.pageYOffset; }
			 * if (document.documentElement &&
			 * document.documentElement.scrollTop) { return
			 * document.documentElement.scrollTop; } if
			 * (document.body.scrollTop){ return
			 * document.body.scrollTop; }
			 * 
			 * return 0; }
			 * 
			 * function elmYPosition(eID) { var elm =
			 * document.getElementById(eID);
			 * 
			 * var y = elm.offsetTop; var node = elm; while
			 * (node.offsetParent && node.offsetParent !=
			 * document.body) { node = node.offsetParent; y +=
			 * node.offsetTop; } return y; }
			 */

		};
		$scope.elmYPosition = function(eID) {
			var elm = document.getElementById(eID);
			if (elm != null && elm != undefined) {
				var y = elm.offsetTop;
				var node = elm;
				while (node.offsetParent
						&& node.offsetParent != document.body) {
					node = node.offsetParent;
					y += node.offsetTop;
				}
				return y;
			}
			return 0;
		};
		$scope.currentYPosition = function() {
			if (self.pageYOffset) {
				return self.pageYOffset;
			}
			if (document.documentElement
					&& document.documentElement.scrollTop) {
				return document.documentElement.scrollTop;
			}
			if (document.body.scrollTop) {
				return document.body.scrollTop;
			}

			return 0;
		};

	});

	// Mode Filter
	project.filter('modeFilter',function() {
		var modeFilter = function(input, selectedMode) {
			var ret = new Array();
			var i = 0;
			angular.forEach(input,function(item, key) {
				ret.push(item);
				i++;
				if (item.read
						&& (selectedMode === "notRead" || selectedMode === "unreadFirst")) {
					ret.splice(i - 1, 1);
					i--;
				}
				;
			});
			angular.forEach(input,function(item, key) {
				if (item.read
						&& selectedMode === "unreadFirst") {
					ret.push(item);
				}
				;
			});

			angular.forEach(input,function(item, key) {
				if (!item.read
						&& selectedMode === "readFirst") {
					ret.splice(i - 1, 1);
					i--;
				}
				;
			});

			angular.forEach(input,function(item, key) {
				if (!item.read
						&& selectedMode === "readFirst") {
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
	var project = angular.module(appName, [ 'ngRoute', 'ngSanitize' ]);

	// config
	project.config([ '$routeProvider', function($routeProvider) {
		$routeProvider.when('/', {
			controller : 'editCtrl',
			templateUrl : appHomePath + '/views/edit.html'
		}).otherwise({
			redirectTo : '/'
		});
	} ]);

	project.controller('editCtrl', function($scope, $http) {

		refreshJSON();

		// get context as JSON
		function refreshJSON() {
			$http({
				method : 'GET',
				url : url(encodeUrl(resourceURL), "getEditJSON")
			}).success(function(data) {
				// i18n messages
				$scope.msgs = data.messages;
				// context
				$scope.ctx = data.context;
				// categories
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
			$http(
					{
						method : 'GET',
						url : url(encodeUrl(resourceURL),
								"toogleCategorySubcribtion", $scope.ctx.id,
								cat.id, cat.subscribed)
					}).success(function(data) {
						cat.subscribed = !cat.subscribed;
						cat.notSubscribed = !cat.notSubscribed;
						refreshJSON();
					});
		};

		$scope.toogleSourceSubcribtion = function(cat, src) {
			$http(
					{
						method : 'GET',
						url : url(encodeUrl(resourceURL),
								"toogleSourceSubcribtion", cat.id, src.id,
								src.subscribed)
					}).success(function(data) {
						src.subscribed = !src.subscribed;
						src.notSubscribed = !src.notSubscribed;
					});
		};
	});

};

//************* utils *************

function encodeUrl(requestUrl) {
	return requestUrl.replace(/%40/gi, '@').replace(/%3A/gi, ':').replace(
			/%24/g, '$').replace(/%2C/gi, ',');
}

//forge a portlet resource url
function url(pattern, id, p1, p2, p3, p4) {
	return pattern.replace("@@id@@", id).replace("__p1__", p1).replace(
			"__p2__", p2).replace("__p3__", p3).replace("__p4__", p4);
}
