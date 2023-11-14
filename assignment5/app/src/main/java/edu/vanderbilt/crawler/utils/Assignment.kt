package edu.vanderbilt.crawler.utils

import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.app.App

object Assignment {
    fun getMainMenu(): Int =
        if (App.instance.resources.assets.list("")?.contains("solution") == true) {
            R.menu.menu_main_solution
        } else {
            R.menu.menu_main
        }
}