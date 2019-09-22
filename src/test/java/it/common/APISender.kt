package it.common

import brs.common.TestInfrastructure
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays

class APISender {

    private val httpclient: HttpClient
    private val parser = JsonParser()

    init {
        httpclient = HttpClientBuilder.create().build()
    }

    fun retrieve(requestType: String, extraParams: List<BasicNameValuePair>): JsonObject {
        val post = HttpPost("/burst")

        val urlParameters = mutableListOf<NameValuePair>()
        urlParameters.add(BasicNameValuePair("requestType", requestType))
        urlParameters.add(BasicNameValuePair("random", "0.7113466594385798"))
        urlParameters.addAll(extraParams)

        post.entity = UrlEncodedFormEntity(urlParameters)

        val result = httpclient.execute(HttpHost("localhost", TestInfrastructure.TEST_API_PORT), post)

        return parser.parse(EntityUtils.toString(result.entity, "UTF-8")) as JsonObject
    }

    fun getAccount(accountName: String): JsonObject {
        return retrieve("getAccount", listOf(BasicNameValuePair(ACCOUNT_PARAMETER, accountName), BasicNameValuePair(FIRST_INDEX_PARAMETER, "0"), BasicNameValuePair(LAST_INDEX_PARAMETER, "100"))
        )
    }

}
