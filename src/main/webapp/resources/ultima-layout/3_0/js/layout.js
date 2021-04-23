/** 
 * PrimeFaces Ultima Layout
 */
PrimeFaces.widget.Ultima = PrimeFaces.widget.BaseWidget.extend({
    
    init: function(cfg) {
        this._super(cfg);
        this.wrapper = $(document.body).find('.layout-wrapper');
        this.topbar = this.wrapper.find('.topbar');
        this.menu = this.jq;
        this.menuWrapper = this.menu.closest('.layout-menu');
        this.menulinks = this.menu.find('a');
        this.expandedMenuitems = this.expandedMenuitems || [];
        this.rightPanel = this.wrapper.children('.layout-rightpanel');
        this.layoutMask = this.wrapper.children('.layout-mask');
        this.profileButton = $('#profile-options');
        this.profileMenu = $('#profile-menu');
        this.topbarItems = this.topbar.find('.topbar-items');
        this.topbarLinks = this.topbarItems.find('> li > a');
        this.menuButton = $('#menu-button');
        this.topbarMenuButton = $('#topbar-menu-button');
        this.rightPanelButton = $('.rightpanel-btn');
        this.menuActive = false;
        this.topbarLinkClick = false;
        this.topbarMenuClick = false;
        this.menuClick = false;
        this.menuButtonClick = false;
        this.isMobileDev = this.isMobileDevice();
        this.isRTL = this.wrapper.hasClass('layout-rtl');
        
        if(this.wrapper.hasClass('layout-menu-slim')) {
            this.profileButton = $('.profile');
        }

        this._bindEvents();
        
        if(this.cfg.stateful && !this.wrapper.hasClass('menu-layout-horizontal') && !this.wrapper.hasClass('layout-menu-slim')) {
            this.restoreMenuState();
        }
        
        var $this = this;
        // noinspection JSValidateTypes
        this.menuWrapper.children('.nano').nanoScroller({flash:true, isRTL:$this.isRTL});
    },
    
    _bindEvents: function() {
        var $this = this;
        
        this.menuWrapper.on('click', function(e) {
            $this.menuClick = true;
        });
        
        this.menuButton.off('click.menuButton').on('click.menuButton', function(e) {
            $this.menuButton.toggleClass('menu-button-rotate');
            $this.topbarItems.removeClass('topbar-items-visible');
            $this.menuButtonClick = true;
            
            //overlay
            if($this.wrapper.hasClass('menu-layout-overlay')) {
                $this.wrapper.toggleClass('layout-menu-overlay-active');
                
                if($this.wrapper.hasClass('layout-menu-overlay-active')) {
                    $this.enableModal();
                    $this.enableSwipe();
                }
                else {
                    $this.disableModal();
                    $this.disableSwipe();
                }
            }
            //static
            else {
                if($this.isDesktop()) {
                    $this.wrapper.toggleClass('layout-menu-static-inactive')
                }
                else {
                    if($this.wrapper.hasClass('layout-menu-static-active')) {
                        $this.wrapper.removeClass('layout-menu-static-active');
                        $this.disableModal();
                        $this.disableSwipe();
                    }
                    else {
                        $this.wrapper.addClass('layout-menu-static-active');
                        $this.wrapper.removeClass('layout-menu-static-inactive');
                        $this.enableModal();
                        $this.enableSwipe();
                    }
                }
                
                setTimeout(function() {
                    $(window).trigger('resize');
                }, 200);  
            }
            
            e.preventDefault();
        });
        
        this.topbarMenuButton.off('click.topbarButton').on('click.topbarButton', function(e) {
            $this.topbarMenuClick = true;
            $this.topbarItems.find('ul').removeClass('fadeInDown fadeOutUp');
            $this.closeOverlayMenu();

            if($this.topbarItems.hasClass('topbar-items-visible')) {
                $this.topbarItems.addClass('fadeOutUp');
                
                setTimeout(function() {
                    $this.topbarItems.removeClass('fadeOutUp topbar-items-visible');
                },500);
            }
            else {
                $this.topbarItems.addClass('topbar-items-visible fadeInDown');
            }
            
            $this.rightPanel.removeClass('layout-rightpanel-active');
            $this.rightPanelButton.removeClass('rightpanel-btn-active');
            
            e.preventDefault();
        });
        
        this.rightPanelButton.on('click', function(e) {
            $this.rightPanelButtonClick = true;
            $this.rightPanel.toggleClass('layout-rightpanel-active');
            $this.closeOverlayMenu();
            
            if($this.rightPanel.hasClass('layout-rightpanel-active')) {
                $this.rightPanel.find('.nano').nanoScroller({flash:true});
            }
            
            e.preventDefault();
        });
        
        this.rightPanel.on('click', function(e) {
            $this.rightPanelClick = true;
        });
        
        this.menulinks.off('click').on('click', function(e) {
            var link = $(this),
            item = link.parent(),
            submenu = item.children('ul'),
            horizontal = $this.isHorizontal() && $this.isDesktop();
            
            if($this.isSlim() && item.parent().hasClass('ultima-menu')) {
                if(item.hasClass('active-menuitem')) {
                    if(submenu.length) {
                        $this.removeMenuitem(item.attr('id'));
                        item.removeClass('active-menuitem');
                        submenu.hide();
                    }
                    
                    if(item.parent().is($this.jq)) {
                        $this.menuActive = false;
                    }
                }
                else {
                    item.addClass('active-menuitem');
                    $this.addMenuitem(item.attr('id'));
                    $this.deactivateItems(item.siblings(), false);
                    submenu.show();

                    $this.updateSubPosOnSlimMenu(submenu, item);
                    
                    if(item.parent().is($this.jq)) {
                        $this.menuActive = true;
                    }
                }
            }
            else {
                if(item.hasClass('active-menuitem')) {
                    if(submenu.length) {
                        $this.removeMenuitem(item.attr('id'));
                        item.removeClass('active-menuitem');
                        
                        if(horizontal) {
                            if(item.parent().is($this.jq)) {
                                $this.menuActive = false;
                            }
                            
                            submenu.hide();
                        }
                        else {
                            submenu.slideUp();
                        }
                    }
                }
                else {
                    $this.addMenuitem(item.attr('id'));
                    
                    if(horizontal) {
                        $this.deactivateItems(item.siblings());
                        item.addClass('active-menuitem');
                        $this.menuActive = true;
                        submenu.show();
                    }
                    else {
                        $this.deactivateItems(item.siblings(), true);
                        $this.activate(item);
                    }
                }
                
                if($this.isSlim()) {
                    var activeParentItem = submenu.parents('.active-menuitem:last');
                    setTimeout(function() {
                        $this.updateSubPosOnSlimMenu(activeParentItem.children('ul'), activeParentItem);
                    }, 350);
                }
            }
            
            if(!horizontal) {
                setTimeout(function() {
                    $(".nano").nanoScroller();
                }, 500);
            }
                                    
            if(submenu.length) {
                e.preventDefault();
            }
        });
        
        this.menu.find('> li').on('mouseenter', function(e) {    
            if(($this.isHorizontal() && $this.isDesktop()) || $this.isSlim()) {
                var item = $(this);
                
                if(!item.hasClass('active-menuitem')) {
                    $this.menu.find('.active-menuitem').removeClass('active-menuitem');
                    $this.menu.find('ul:visible').hide();
                    $this.menu.find('.ink').remove();
                    
                    if($this.menuActive) {
                        item.addClass('active-menuitem');
                        item.children('ul').show();
                    }
                }
            }
        });
        
        this.profileButton.off('click.profileButton').on('click.profileButton', function(e) {
            var profile = $this.profileMenu.prev('.profile'),
            expanded = profile.hasClass('profile-expanded');
            
            if($this.isSlim()) {
                $this.deactivateItems($this.menu.children('.active-menuitem'), false);
            }
            
            $this.profileMenu.slideToggle();
            
            $this.profileMenu.prev('.profile').toggleClass('profile-expanded');
            
            if($this.cfg.stateful) {
                $this.setInlineProfileState(!expanded);
            }
            
            setTimeout(function() {
                $(".nano").nanoScroller();
            }, 500);
            
            e.preventDefault();
        });
        
        this.topbarLinks.off('click.topbarLink').on('click.topbarLink', function(e) {
            var link = $(this),
            item = link.parent(),
            submenu = link.next();
            
            $this.topbarLinkClick = true;

            item.siblings('.active-top-menu').removeClass('active-top-menu');
            $this.closeOverlayMenu();

            if($this.isDesktop()) {
                if(submenu.length) {
                    if(item.hasClass('active-top-menu')) {
                        submenu.addClass('fadeOutUp');
                        
                        setTimeout(function() {
                            item.removeClass('active-top-menu'),
                            submenu.removeClass('fadeOutUp');
                        },500);
                    }
                    else {
                        item.addClass('active-top-menu');
                        submenu.addClass('fadeInDown');
                    }
                }
            }
            else {
                item.children('ul').removeClass('fadeInDown fadeOutUp');
                item.toggleClass('active-top-menu');
            }
            
            var href = link.attr('href');
            if(href && href !== '#') {
                window.location.href = href;
            }
            
            e.preventDefault();         
        });
        
        $this.topbarItems.children('.search-item').on('click', function(e) {
            $this.topbarLinkClick = true;
        });
        
        $(document.body).off('click').on('click', function(e) {
            if(($this.isHorizontal() || $this.isSlim()) && !$this.menuClick && $this.isDesktop()) {
                $this.menu.find('.active-menuitem').removeClass('active-menuitem');
                $this.menu.find('ul:visible').hide();
                $this.menuActive = false;
            }
            
            if(!$this.topbarMenuClick && !$this.topbarLinkClick) {
                $this.topbarItems.find('.active-top-menu').removeClass('active-top-menu');
            }
            
            if(!$this.menuClick && $this.isSlim()) {
                $this.deactivateItems($this.menu.children('.active-menuitem'), false);
            }
            
            if(!$this.rightPanelClick && !$this.rightPanelButtonClick && $this.rightPanel.hasClass('layout-rightpanel-active') && !$this.isDatePickerPanelClicked() && !$this.isOverlayInputPanelClicked(e)) {
                $this.rightPanel.removeClass('layout-rightpanel-active');
                $this.rightPanelButton.removeClass('rightpanel-btn-active');
            }
            
            if(!$this.topbarMenuClick && !$this.topbarLinkClick) {
                $this.topbarItems.removeClass('topbar-items-visible');
            }
                        
            $this.menuClick = false;
            $this.menuButtonClick = false;
            $this.topbarLinkClick = false;
            $this.topbarMenuClick = false;
            $this.rightPanelClick = false;
            $this.rightPanelButtonClick = false;
        });
    },

    isDatePickerPanelClicked: function () {
        if ($.datepicker) {
            var input = $($.datepicker._lastInput);
            if (input.closest('.layout-rightpanel').length && $('#ui-datepicker-div').is(':visible')) {
                return true;
            }
        }
        return false;
    },
    
    isOverlayInputPanelClicked: function(e) {
        var el = $(e.target),
        panel = el.closest('.ui-input-overlay');
        if(panel.length) {
            var inputId = panel.attr('id').replace(/_panel/g, '');
            var input = $(PrimeFaces.escapeClientId(inputId));

            if(input.length && input.closest('.layout-rightpanel').length) {
                return true;
            }
        }
        return false;
    },
    
    updateSubPosOnSlimMenu: function(submenu, item) {
        submenu.removeClass('submenu-top');
        
        var win = $(window),
        h = win.innerHeight()||$(document).height(),
        top = item.offset().top - win.scrollTop(),
        submenuHeight = submenu.height();

        if(top + submenuHeight > h && top >= submenuHeight) {
            submenu.addClass('submenu-top');
        }
    },
    
    closeOverlayMenu: function() {
        var $this = this;
        
        if($this.wrapper.hasClass('layout-menu-overlay-active')||$this.wrapper.hasClass('layout-menu-static-active')) {
            $this.menuButton.removeClass('menu-button-rotate');
            $this.wrapper.removeClass('layout-menu-overlay-active layout-menu-static-active');
            $this.disableModal();
        }
    },
    
    activate: function(item) {
        var submenu = item.children('ul');
        item.addClass('active-menuitem');

        if(submenu.length) {
            submenu.slideDown();
        }
    },
    
    deactivate: function(item) {
        var submenu = item.children('ul');
        item.removeClass('active-menuitem');
        
        if(submenu.length) {
            submenu.hide();
        }
    },
        
    deactivateItems: function(items, animate) {
        var $this = this;
        
        for(var i = 0; i < items.length; i++) {
            var item = items.eq(i),
            submenu = item.children('ul');
            
            if(submenu.length) {
                if(item.hasClass('active-menuitem')) {
                    var activeSubItems = item.find('.active-menuitem');
                    item.removeClass('active-menuitem');
                    item.find('.ink').remove();
                    
                    if(animate) {
                        submenu.slideUp('normal', function() {
                            $(this).parent().find('.active-menuitem').each(function() {
                                $this.deactivate($(this));
                            });
                        });
                    }
                    else {
                        submenu.hide();
                        item.find('.active-menuitem').each(function() {
                            $this.deactivate($(this));
                        });
                    }
                    
                    $this.removeMenuitem(item.attr('id'));
                    activeSubItems.each(function() {
                        $this.removeMenuitem($(this).attr('id'));
                    });
                }
                else {
                    item.find('.active-menuitem').each(function() {
                        var subItem = $(this);
                        $this.deactivate(subItem);
                        $this.removeMenuitem(subItem.attr('id'));
                    });
                }
            }
            else if(item.hasClass('active-menuitem')) {
                $this.deactivate(item);
                $this.removeMenuitem(item.attr('id'));
            }
        }
    },
            
    removeMenuitem: function (id) {
        this.expandedMenuitems = $.grep(this.expandedMenuitems, function (value) {
            return value !== id;
        });
        
        if(this.cfg.stateful) {
            this.saveMenuState();
        }
    },
    
    addMenuitem: function (id) {
        if ($.inArray(id, this.expandedMenuitems) === -1) {
            this.expandedMenuitems.push(id);
        }
        
        if(this.cfg.stateful) {
            this.saveMenuState();
        }
    },
    
    saveMenuState: function() {
        $.cookie('ultima_expandeditems', this.expandedMenuitems.join(','), {path: '/'});
    },
    
    clearMenuState: function() {
        $.removeCookie('ultima_expandeditems', {path: '/'});
    },
    
    setInlineProfileState: function(expanded) {
        if(expanded)
            $.cookie('ultima_inlineprofile_expanded', "1", {path: '/'});
        else
            $.removeCookie('ultima_inlineprofile_expanded', {path: '/'});
    },
    
    restoreMenuState: function() {
        var menucookie = $.cookie('ultima_expandeditems');
        if (menucookie) {
            this.clearActiveItems();
            
            this.expandedMenuitems = menucookie.split(',');
            for (var i = 0; i < this.expandedMenuitems.length; i++) {
                var id = this.expandedMenuitems[i];
                if (id) {
                    var menuitem = $("#" + this.expandedMenuitems[i].replace(/:/g, "\\:"));
                    menuitem.addClass('active-menuitem');
                    
                    var submenu = menuitem.children('ul');
                    if(submenu.length) {
                        submenu.show();
                    }
                }
            }
        }
        
        var inlineProfileCookie = $.cookie('ultima_inlineprofile_expanded');
        if (inlineProfileCookie) {
            this.profileMenu.show().prev('.profile').addClass('profile-expanded');
        }
    },
    
    enableModal: function() {
        var $this = this;
        var mask = $('<div class="layout-mask"></div>').on('click', function() {
            $this.wrapper.removeClass('layout-menu-static-active');
            $this.wrapper.removeClass('layout-menu-overlay-active');
            $this.menuButton.removeClass('menu-button-rotate');
            $this.disableModal();
        });
        this.modal = this.wrapper.append(mask).children('.layout-mask');
    },
    
    disableModal: function() {
        this.modal.remove();
    },
    
    enableSwipe: function() {
        if(this.isMobileDev) {
            var $this = this;
            this.menuWrapper.swipe({
                swipeLeft: function() {
                    $this.menuButton.click();
                }
            });
        }
    },
    
    disableSwipe: function() {
        if(this.isMobileDev) {
            this.menuWrapper.swipe('destroy');
        }
    },
    
    isHorizontal: function() {
        return this.wrapper.hasClass('menu-layout-horizontal');
    },
    
    isSlim: function() {
        return this.isDesktop() && this.wrapper.hasClass('layout-menu-slim');
    },
    
    isTablet: function() {
        var width = window.innerWidth;
        return width <= 1024 && width > 640;
    },

    isDesktop: function() {
        return window.innerWidth > 1024;
    },

    isMobile: function() {
        return window.innerWidth <= 640;
    },
    
    isMobileDevice: function() {
        return /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(window.navigator.userAgent.toLowerCase());
    },
    
    clearActiveItems: function() {
        var activeItems = this.jq.find('li.active-menuitem'),
        subContainers = activeItems.children('ul');

        activeItems.removeClass('active-menuitem');
        if(subContainers && subContainers.length) {
            subContainers.hide();
        }
    },
    
    rightPanelOpen: function() {
        if(this.rightPanel.length) {
            this.rightPanel.addClass('layout-rightpanel-active');
            this.closeOverlayMenu();
            
            if(this.rightPanel.hasClass('layout-rightpanel-active')) {
                this.rightPanel.find('.nano').nanoScroller({flash:true});
            }
        }
    },
    
    rightPanelClose: function() {
        if(this.rightPanel.length) {
            this.rightPanel.removeClass('layout-rightpanel-active');
            this.closeOverlayMenu();
        }
    }
    
});

/*!
 * jQuery Cookie Plugin v1.4.1
 * https://github.com/carhartl/jquery-cookie
 *
 * Copyright 2006, 2014 Klaus Hartl
 * Released under the MIT license
 */
(function (factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD (Register as an anonymous module)
		define(['jquery'], factory);
	} else if (typeof exports === 'object') {
		// Node/CommonJS
		module.exports = factory(require('jquery'));
	} else {
		// Browser globals
		factory(jQuery);
	}
}(function ($) {

	var pluses = /\+/g;

	function encode(s) {
		return config.raw ? s : encodeURIComponent(s);
	}

	function decode(s) {
		return config.raw ? s : decodeURIComponent(s);
	}

	function stringifyCookieValue(value) {
		return encode(config.json ? JSON.stringify(value) : String(value));
	}

	function parseCookieValue(s) {
		if (s.indexOf('"') === 0) {
			// This is a quoted cookie as according to RFC2068, unescape...
			s = s.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\');
		}

		try {
			// Replace server-side written pluses with spaces.
			// If we can't decode the cookie, ignore it, it's unusable.
			// If we can't parse the cookie, ignore it, it's unusable.
			s = decodeURIComponent(s.replace(pluses, ' '));
			return config.json ? JSON.parse(s) : s;
		} catch(e) {}
	}

	function read(s, converter) {
		var value = config.raw ? s : parseCookieValue(s);
		return $.isFunction(converter) ? converter(value) : value;
	}

	var config = $.cookie = function (key, value, options) {

		// Write

		if (arguments.length > 1 && !$.isFunction(value)) {
			options = $.extend({}, config.defaults, options);

			if (typeof options.expires === 'number') {
				var days = options.expires, t = options.expires = new Date();
				t.setMilliseconds(t.getMilliseconds() + days * 864e+5);
			}

			return (document.cookie = [
				encode(key), '=', stringifyCookieValue(value),
				options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
				options.path    ? '; path=' + options.path : '',
				options.domain  ? '; domain=' + options.domain : '',
				options.secure  ? '; secure' : ''
			].join(''));
		}

		// Read

		var result = key ? undefined : {},
			// To prevent the for loop in the first place assign an empty array
			// in case there are no cookies at all. Also prevents odd result when
			// calling $.cookie().
			cookies = document.cookie ? document.cookie.split('; ') : [],
			i = 0,
			l = cookies.length;

		for (; i < l; i++) {
			var parts = cookies[i].split('='),
				name = decode(parts.shift()),
				cookie = parts.join('=');

			if (key === name) {
				// If second argument (value) is a function it's a converter...
				result = read(cookie, value);
				break;
			}

			// Prevent storing a cookie that we couldn't decode.
			if (!key && (cookie = read(cookie)) !== undefined) {
				result[name] = cookie;
			}
		}

		return result;
	};

	config.defaults = {};

	$.removeCookie = function (key, options) {
		// Must not alter options, thus extending a fresh object...
		$.cookie(key, '', $.extend({}, options, { expires: -1 }));
		return !$.cookie(key);
	};

}));

/* Issue #924 is fixed for 5.3+ and 6.0. (compatibility with 5.3) */
if(window['PrimeFaces'] && window['PrimeFaces'].widget.Dialog) {
    PrimeFaces.widget.Dialog = PrimeFaces.widget.Dialog.extend({
        
        enableModality: function() {
            this._super();
            $(document.body).children(this.jqId + '_modal').addClass('ui-dialog-mask');
        },
        
        syncWindowResize: function() {}
    });
}

/* JS extensions to support material animations */
if(PrimeFaces.widget.InputSwitch) {
    PrimeFaces.widget.InputSwitch = PrimeFaces.widget.InputSwitch.extend({
         
         init: function(cfg) {
             this._super(cfg);
             
             if(this.input.prop('checked')) {
                 this.jq.addClass('ui-inputswitch-checked');
             }
         },
         
         toggle: function() {
             var $this = this;

             if(this.input.prop('checked')) {
                 this.uncheck(); 
                 setTimeout(function() {
                    $this.jq.removeClass('ui-inputswitch-checked');
                 }, 100);
             }
             else {
                 this.check();
                 setTimeout(function() {
                    $this.jq.addClass('ui-inputswitch-checked');
                 }, 100);
             }
         }
    });
}

if(PrimeFaces.widget.SelectBooleanButton) {
    PrimeFaces.widget.SelectBooleanButton.prototype.check = function() {
        if(!this.disabled) {
            this.input.prop('checked', true);
            this.jq.addClass('ui-state-active').children('.ui-button-text').contents()[0].textContent = this.cfg.onLabel;

            if(this.icon.length > 0) {
                this.icon.removeClass(this.cfg.offIcon).addClass(this.cfg.onIcon);
            }

            this.input.trigger('change');
        }
    };
    
    PrimeFaces.widget.SelectBooleanButton.prototype.uncheck = function() {
        if(!this.disabled) {
            this.input.prop('checked', false);
            this.jq.removeClass('ui-state-active').children('.ui-button-text').contents()[0].textContent = this.cfg.offLabel;

            if(this.icon.length > 0) {
                this.icon.removeClass(this.cfg.onIcon).addClass(this.cfg.offIcon);
            }

            this.input.trigger('change');
        }
    }
}

PrimeFaces.skinInput = function(input) {
    setTimeout(function() {
        if(input.val() !== '') {
            var parent = input.parent();
            input.addClass('ui-state-filled');
            
            if(parent.is("span:not('.md-inputfield')")) {
                parent.addClass('md-inputwrapper-filled');
            }
        }
    }, 1);
    
    input.on('mouseenter', function() {
        $(this).addClass('ui-state-hover');
    })
    .on('mouseleave', function() {
        $(this).removeClass('ui-state-hover');
    })
    .on('focus', function() {
        var parent = input.parent();
        $(this).addClass('ui-state-focus');
        
        if(parent.is("span:not('.md-inputfield')")) {
            parent.addClass('md-inputwrapper-focus');
        }
    })
    .on('blur', function() {
        $(this).removeClass('ui-state-focus');

        if(input.hasClass('hasDatepicker')) {
            setTimeout(function() {
                PrimeFaces.onInputBlur(input);
            },150);
        }
        else {
            PrimeFaces.onInputBlur(input);
        }
    });

    //aria
    input.attr('role', 'textbox')
            .attr('aria-disabled', input.is(':disabled'))
            .attr('aria-readonly', input.prop('readonly'));

    if(input.is('textarea')) {
        input.attr('aria-multiline', true);
    }

    return this;
};

PrimeFaces.onInputBlur = function(input) {
    var parent = input.parent(),
    hasInputFieldClass = parent.is("span:not('.md-inputfield')");
    
    if(parent.hasClass('md-inputwrapper-focus')) {
        parent.removeClass('md-inputwrapper-focus');
    }
    
    if(input.val() !== '') {
        input.addClass('ui-state-filled');
        if(hasInputFieldClass) {
            parent.addClass('md-inputwrapper-filled');
        }
    }
    else {
        input.removeClass('ui-state-filled');
        parent.removeClass('md-inputwrapper-filled');
    }    
};

if(PrimeFaces.widget.AutoComplete) {
    PrimeFaces.widget.AutoComplete.prototype.setupMultipleMode = function() {
        var $this = this;
        this.multiItemContainer = this.jq.children('ul');
        this.inputContainer = this.multiItemContainer.children('.ui-autocomplete-input-token');
        
        if(this.hinput.children().length) {
            this.jq.addClass('md-inputwrapper-filled');
        }

        this.multiItemContainer.hover(function() {
                $(this).addClass('ui-state-hover');
            },
            function() {
                $(this).removeClass('ui-state-hover');
            }
        ).click(function() {
            $this.input.focus();
        });

        //delegate events to container
        this.input.focus(function() {
            $this.multiItemContainer.addClass('ui-state-focus');
            $this.jq.addClass('md-inputwrapper-focus');
        }).blur(function(e) {
            $this.multiItemContainer.removeClass('ui-state-focus');
            $this.jq.removeClass('md-inputwrapper-focus').addClass('md-inputwrapper-filled');
            
            setTimeout(function() {
                if($this.hinput.children().length === 0 && !$this.multiItemContainer.hasClass('ui-state-focus')) {
                    $this.jq.removeClass('md-inputwrapper-filled');
                }
            }, 150); 
        });

        var closeSelector = '> li.ui-autocomplete-token > .ui-autocomplete-token-icon';
        this.multiItemContainer.off('click', closeSelector).on('click', closeSelector, null, function(event) {
            if($this.multiItemContainer.children('li.ui-autocomplete-token').length === $this.cfg.selectLimit) {
                if(PrimeFaces.isIE(8)) {
                    $this.input.val('');
                }
                $this.input.css('display', 'inline');
                $this.enableDropdown();
            }
            $this.removeItem(event, $(this).parent());
        });
    };
}

if(PrimeFaces.widget.Calendar) {
    PrimeFaces.widget.Calendar.prototype.bindDateSelectListener = function() {
        var _self = this;

        this.cfg.onSelect = function() {
            if(_self.cfg.popup) {
                _self.fireDateSelectEvent();
            }
            else {
                var newDate = $.datepicker.formatDate(_self.cfg.dateFormat, _self.getDate());

                _self.input.val(newDate);
                _self.fireDateSelectEvent();
            }
            
            if(_self.input.val() !== '') {
               var parent = _self.input.parent();
               parent.addClass('md-inputwrapper-filled');
               _self.input.addClass('ui-state-filled');
           }
        };
    };
}

if(PrimeFaces.widget.SelectOneMenu) {
    PrimeFaces.widget.SelectOneMenu = PrimeFaces.widget.SelectOneMenu.extend({
        init: function(cfg) {
            this._super(cfg);

            var $this = this;
            if(!this.disabled && this.jq.parent().hasClass('md-inputfield')) {
                this.itemsContainer.children('.ui-selectonemenu-item:first').css({'display': 'none'});
                if (this.input.val() !== "") {
                    this.jq.addClass("ui-state-filled");
                }

                this.input.off('change').on('change', function() {
                    $this.inputValueControl($(this));
                });
                
                if(this.cfg.editable) {
                    this.label.on('input', function(e) {
                        $this.inputValueControl($(this));
                    }).on('focus', function() {
                        $this.jq.addClass('ui-state-focus');
                    }).on('blur', function() {
                        $this.jq.removeClass('ui-state-focus');
                        $this.inputValueControl($(this));
                    });
                }
            }
        },
        
        inputValueControl: function(input) {
            if (input.val() !== "")
                this.jq.addClass("ui-state-filled"); 
            else
                this.jq.removeClass("ui-state-filled");
        }
    });
}