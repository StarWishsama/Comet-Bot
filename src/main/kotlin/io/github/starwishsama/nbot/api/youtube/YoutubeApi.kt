package io.github.starwishsama.nbot.api.youtube

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import java.io.IOException
import java.io.StringReader
import java.security.GeneralSecurityException


object YoutubeApi {
    private val SCOPES: Collection<String> =
        listOf("https://www.googleapis.com/auth/youtube.readonly")

    private const val APPLICATION_NAME = "API code samples"
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun authorize(httpTransport: NetHttpTransport?): Credential {
        // Load client secrets.
        val clientSecrets = GoogleClientSecrets()
        val installed = GoogleClientSecrets.Details()
        installed.clientId
        installed.clientSecret = "AIzaSyBSxKymrrQpNUIsC2lqupHlHxjvRGoPv90"
        clientSecrets.installed = installed
        // Build flow and trigger user authorization request.
        val flow =
            GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .build()
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun getService(): YouTube? {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential: Credential = authorize(httpTransport)
        return YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    @Throws(GeneralSecurityException::class, IOException::class, GoogleJsonResponseException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val youtubeService = getService()
        // Define and execute the API request
        val request = youtubeService!!.channels()
            .list("snippet,contentDetails,statistics")
        val response = request.setForUsername("SuiseiChannel").execute()
        println(response.items[0])
    }
}