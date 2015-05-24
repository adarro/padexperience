package org.adarro.padx

import org.adarro.padx.padobjects.CardMeta
import com.couchbase.lite.Context
import com.couchbase.lite.Manager
import com.couchbase.lite.JavaContext
package object db {
  object Database {

    private lazy val mContext: Context = getApplicationContext
    val manager: Manager = new Manager(mContext,
      Manager.DEFAULT_OPTIONS)
    def getApplicationContext: Context = new JavaContext("data")
  }

  def data = Database.manager.getDatabase("padx")
}