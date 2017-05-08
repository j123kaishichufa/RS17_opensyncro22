function itemMouseOver(e) {
  var target = e.getTarget();
  if(target.menu.openSubMenu)
    target.menu.openSubMenu.close();
  if(target.subMenu) {
    target.openSubMenu();
  }
  target.setBgColor(target.appearance.bgHiliteColor);
}

function itemMouseOut(e) {
  var target = e.getTarget();
  target.setBgColor(target.appearance.bgrColor);
}

function itemMouseDown(e) {
  var target = e.getTarget();
  if(target.linkJavaScript)
      eval(target.linkJavaScript);
  else
      target.menu.menuPage.doc.location.replace(target.link);

  if(target.menu.menuContainer.openRootMenu)
      target.menu.menuContainer.openRootMenu.close();	
}


function MenuItem(text, link) {
  this.DynLayer=DynLayer;
  this.DynLayer();
  this.hasSubMenu = false;
  
  this.text = text || '';
  if(link) {
      this.link = link;
      if(link.length >= 11 && link.substring(0, 11) == "javascript:")
	  this.linkJavaScript = link.substring(11, link.length);
  }
  var listener = new EventListener(this);
  listener.onmouseover = itemMouseOver;
  
  listener.onmouseout = itemMouseOut;
  if(link)
    listener.onmousedown = itemMouseDown;
  
  this.addEventListener(listener);
}


MenuItem.prototype = new DynLayer;

MenuItem.prototype.setText = function(newText) {
    if(newText)
      this.text = newText;
    
    if(this.appearance) {
	var style = 'STYLE="'+(this.link || this.hasSubMenu ? this.appearance.font.enabledStyle : this.appearance.font.disabledStyle)+'"';
	var html = '<table width="100%" cellpadding=2 cellspacing=0 border=0><tr><td ><P '+style+'>'+this.text+'</P></td>';
	if(this.hasSubMenu)
	    html += '<td align="right">'+this.appearance.subMenuMark+'</td>';
	html += '</tr></table>';
	this.setHTML(html);
    }
    else
	alert("setText called, but no appearance found");
}

MenuItem.prototype.setAppearance = function(appearance) {
    this.appearance = appearance;
    this.setHeight(appearance.itemHeight);
    this.setBgColor(appearance.bgrColor);
    this.setText();
    if(this.subMenu)
      this.subMenu.setAppearance(appearance);
}

MenuItem.prototype.setSubMenu = function(subMenu) {
    this.subMenu = subMenu;
    this.hasSubMenu = true;
    this.subMenuPositioned = false;
    return this;
}

MenuItem.prototype.openSubMenu = function() {
    this.subMenu.moveTo(this.menu.getX() + this.getWidth() - 5, this.menu.getY() + this.getY());
    this.subMenu.setVisible(true);
    this.menu.openSubMenu = this.subMenu;
}


function Menu(width) {
    this.DynLayer=DynLayer;
    this.DynLayer();
    this.menuPage = DynAPI.document;
    this.items = [];
    if(width)
	this.setWidth(width);
    this.setVisible(false);
}

Menu.prototype = new DynLayer();

Menu.prototype.setMenuContainer = function(menuContainer) {
    this.menuContainer = menuContainer;
    this.menuPage = menuContainer.doc;	
    this.menuPage.addChild(this);
    this.setVisible(false);
    var i;
    for(i=0; i<this.items.length; i++) {
	if(this.items[i].subMenu) {
	    this.items[i].subMenu.setMenuContainer(menuContainer);
	}
    }
}

Menu.prototype.setAppearance = function(appearance) {
    this.appearance = appearance;
    this.setBgColor(appearance.borderColor);
    var i;
    for(i=0; i<this.items.length; i++) {
	this.items[i].setWidth(this.w - 2 * this.appearance.borderWidth);
	this.items[i].setAppearance(appearance);
    }
    this.arrangeItems();
}

Menu.prototype.add = function(item) {
    item.menu = this;
    this.items[this.items.length] = item;
    this.addChild(item);
    item.setVisible(true);
    return this;
}

Menu.prototype.arrangeItems = function() {
    var totalHeight = 0;
    var i;
    for(i=0; i<this.items.length; i++) {
	this.items[i].moveTo(this.appearance.borderWidth, this.appearance.borderWidth + totalHeight);
	totalHeight += this.items[i].getHeight();
    }
    this.setHeight(totalHeight + 2 * this.appearance.borderWidth);
}


Menu.prototype.close = function() {
    if(this.openSubMenu) {
	this.openSubMenu.close();
	this.openSubMenu = null;
    }
    this.setVisible(false);
    if(this.menuContainer) {
	if(this.menuContainer.openRootMenu == this)
	    this.menuContainer.openRootMenu = null;
    }
}

Menu.prototype.open = function() {
    if(this.menuContainer && this.menuContainer.areMenusLoaded) {
	if(this.menuContainer.openRootMenu) {
	    if(this.menuContainer.openRootMenu == this)
	        this.close();
	    else {
		this.menuContainer.openRootMenu.close();
		this.menuContainer.openRootMenu = this;
	    }
	}
	else
	    this.menuContainer.openRootMenu = this;
	
	if(this.menuContainer.openRootMenu) {
	    this.menuContainer.openRootMenu.setY(this.menuContainer.y + this.menuContainer.findScrollTop());
	    this.menuContainer.openRootMenu.setVisible(true);
	}
	return false;
    }
    return true;
}

function openMenu(menuObject) {
    if(menuObject) {
	return menuObject.open();
    }
    return false;
}

function MenuAppearance(itemHeight, enabledStyle, disabledStyle, bgrColor, bgHiliteColor, borderColor, borderWidth, subMenuMark) {
    this.itemHeight = itemHeight || 17;
    this.font = {};
    this.font.enabledStyle = enabledStyle || "font-family: helvetica; font-size: 13px; color: black";
    this.font.disabledStyle = disabledStyle || "font-family: helvetica; font-size: 13px; color: #808080";
    this.bgrColor = bgrColor || "#C0C0C0";
    this.bgHiliteColor = bgHiliteColor || "#E0E0E0";
    this.borderColor = borderColor || "black";
    this.borderWidth = borderWidth || 1;
    this.subMenuMark = subMenuMark || "+";
}

function MenuContainer(appearance, y, menuWindow) {
    this.appearance = appearance;
    this.y = y || 0;
    if(menuWindow) {
	this.menuWindow = menuWindow;
	this.doc = new DynDocument(menuWindow);
	DynAPI.addChild(this.doc);
    }
    else {
	this.menuWindow = window;
	this.doc = DynAPI.document;
    }
    this.menus = [];
    this.openRootMenu = null;
    this.areMenusLoaded = false;
    
    this.addRootMenu = addRootMenu;
    this.resetMenus = resetMenus;
    this.closeMenus = closeMenus;
    this.menusLoaded = function() { this.areMenusLoaded = true; };
    this.unloadMenus = unloadMenus;
    this.findScrollTop = findScrollTop;
}

function addRootMenu(menu, x) {
    this.menus[this.menus.length] = menu;
    menu.setAppearance(this.appearance);
    menu.setMenuContainer(this);
   
    //menu.arrangeItems();
   
    menu.menuContainer = this;
    menu.moveTo(x, this.y);
    return this;
}

function resetMenus() {
    this.openRootMenu = null;
}

function closeMenus() {
  if(this.openRootMenu)
      this.openRootMenu.close();
}

function findScrollTop() {
    if (this.menuWindow.pageYOffset != null)
	return this.menuWindow.pageYOffset;
    if (this.menuWindow.document.body.scrollTop != null)
	return this.menuWindow.document.body.scrollTop;
    if (this.menuWindow.document.documentElement.scrollTop != null)
	return this.menuWindow.document.documentElement.scrollTop;
    return (null);
}


function unloadMenus() {
    if(DynAPI != null) {
	if(DynAPI.document.children.length>0) {
	    var index  =  DynAPI.document.children.length-1;
	    DynAPI.document.children[index].deleteFromParent();
	}
    }
    this.areMenusLoaded = false;
}







