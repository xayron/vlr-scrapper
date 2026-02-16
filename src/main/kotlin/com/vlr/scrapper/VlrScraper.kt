package com.vlr.scrapper

import com.vlr.scrapper.model.Match
import com.vlr.scrapper.model.Event
import com.vlr.scrapper.model.TeamRanking
import com.vlr.scrapper.model.PlayerStat
import com.vlr.scrapper.model.RegionRankings
import com.vlr.scrapper.model.NewsItem
import com.vlr.scrapper.model.Transfer

import com.vlr.scrapper.model.Team
import com.vlr.scrapper.model.TeamPlayer
import com.vlr.scrapper.model.TeamMatch
import com.vlr.scrapper.model.EventPlacement
import com.vlr.scrapper.model.PlacementResult
import com.vlr.scrapper.model.RatingHistoryEntry
import com.vlr.scrapper.model.FormRating
import com.vlr.scrapper.model.RankingHistoryEntry
import com.vlr.scrapper.model.Player
import com.vlr.scrapper.model.PastTeam
import com.vlr.scrapper.model.LiveMatch
import com.vlr.scrapper.model.EventDetail
import com.vlr.scrapper.model.EventTeam
import com.vlr.scrapper.model.EventBracketGroup
import com.vlr.scrapper.model.EventBracketRound
import com.vlr.scrapper.model.EventBracketMatch
import com.vlr.scrapper.model.EventBracketTeam
import com.vlr.scrapper.model.TeamSearchResult
import com.vlr.scrapper.model.PlayerSearchResult
import com.vlr.scrapper.model.StreamLink
import com.vlr.scrapper.model.MatchDetail
import com.vlr.scrapper.model.TeamInfo
import com.vlr.scrapper.model.StreamInfo
import com.vlr.scrapper.model.MapResult

import com.vlr.scrapper.model.TeamScore
import com.vlr.scrapper.model.PlayerMatchStat
import com.vlr.scrapper.model.SideStat
import com.vlr.scrapper.model.MapStatsData
import com.vlr.scrapper.model.MapStats
import com.vlr.scrapper.model.Round
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * VLR.gg web scraper for Valorant esports data
 *
 * Provides methods to scrape various types of data from VLR.gg including:
 * - Matches (upcoming, completed, live)
 * - Events and tournaments
 * - Team and player information
 * - Rankings and statistics
 * - News and transfers
 */
class VlrScraper {

    private val baseUrl = "https://www.vlr.gg"

    private fun createConnection(url: String): org.jsoup.Connection {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept-Language", "en-US,en;q=0.9")
            .cookie("abok", "1")
            .timeout(30000)
    }

    /**
     * Extracts country code from a CSS class string
     *
     * @param classString CSS class string containing country code (e.g., "flag mod-us")
     * @return Country code or null if not found
     */
    private fun extractCountryCode(classString: String): String? {
        val match = Regex("mod-([a-z]+)").find(classString)
        return match?.groupValues?.get(1)
    }

    /**
     * Extracts and normalizes event icon URL from element
     *
     * @param element Element containing the event icon
     * @param selector CSS selector for the icon image
     * @return Normalized event icon URL or null if not found
     */
    private fun extractEventIcon(element: Element, selector: String = ".match-item-icon img"): String? {
        val eventIconSrc = element.select(selector).attr("src")
        return if (eventIconSrc.isNotEmpty()) {
            if (eventIconSrc.startsWith("//")) "https:$eventIconSrc" else eventIconSrc
        } else null
    }

    /**
     * Retrieves upcoming matches for a specific page
     *
     * @param page Page number to fetch
     * @return List of upcoming matches
     */
    fun getUpcomingMatches(page: String): List<Match> {
        val doc: Document = createConnection("$baseUrl/matches/?page=$page").get()
        return parseMatches(doc)
    }

    /**
     * Retrieves detailed news article and comments thread
     *
     * @param urlStr Full URL of the news article
     * @return NewsDetail object containing article content and comments
     */
    fun getNewsDetail(urlStr: String): com.vlr.scrapper.model.NewsDetail {
        val doc: Document = createConnection(urlStr)
            .get()

        // Metadata Parsing
        val title = doc.select(".mod-article-title").text().trim()
        val author = doc.select(".article-meta-author").text().trim()
        val dateElem = doc.select(".article-meta .js-date-toggle")
        val date = dateElem.attr("title").ifEmpty { dateElem.text().trim() }
        
        val finalTitle = if (title.isNotEmpty()) title else doc.select("h1").first()?.text() ?: ""
        val finalAuthor = author.ifEmpty { "Unknown" }
        val finalDate = date.ifEmpty { "Unknown" }

        println("Scraping News: $finalTitle ($finalAuthor, $finalDate)")

        // Content Parsing
        val contentElement = doc.selectFirst(".article-body")
        contentElement?.select("style, script")?.remove()
        contentElement?.select("br")?.append("\\n")
        contentElement?.select("p")?.prepend("\\n\\n")
        val content = contentElement?.text()?.trim() ?: ""

        // Comment Parsing (Linear to Tree)
        class MutableComment(
            val author: String,
            val content: String,
            val url: String?,
            val children: MutableList<MutableComment> = mutableListOf()
        ) {
            fun toThreadComment(): com.vlr.scrapper.model.ThreadComment {
               return com.vlr.scrapper.model.ThreadComment(this.author, this.content, url, children.map { it.toThreadComment() })
            }
        }

        val roots = mutableListOf<MutableComment>()
        val lastAtDepth = mutableMapOf<Int, MutableComment>()

        // Try broader selector for posts
        val postElements = doc.select(".wf-card.post")
        
        for ((index, element) in postElements.withIndex()) {
            val depthClass = element.classNames().find { it.startsWith("depth-") }
            val depth = depthClass?.substringAfter("depth-")?.toIntOrNull() ?: 0
            
            val postAuthor = element.select(".post-header-author").text().trim()
            val postContent = element.select(".post-body").text().trim()
            
            // Generate link
            val postLink = element.select(".post-footer .link").attr("href")
            val postUrl = if (postLink.isNotEmpty()) "$baseUrl$postLink" else null
            
            val current = MutableComment(postAuthor, postContent, postUrl)
            lastAtDepth[depth] = current
            
            if (depth == 0) {
                roots.add(current)
            } else {
                // Find parent at depth - 1
                val parent = lastAtDepth[depth - 1]
                if (parent != null) {
                    parent.children.add(current)
                } else {
                    // Fallback: add to nearest upper parent or root
                    var p: MutableComment? = null
                    for (d in depth - 1 downTo 0) {
                         if (lastAtDepth.containsKey(d)) {
                             p = lastAtDepth[d]
                             break
                         }
                    }
                    if (p != null) {
                        p.children.add(current)
                    } else {
                        roots.add(current)
                    }
                }
            }
        }

        return com.vlr.scrapper.model.NewsDetail(finalTitle, finalAuthor, finalDate, content, urlStr, roots.map { it.toThreadComment() })
    }

    /**
     * Retrieves completed matches for a specific page
     *
     * @param page Page number to fetch (default: 1)
     * @return List of completed matches
     */
    fun getCompletedMatches(page: Int = 1): List<Match> {
        val url = if (page > 1) "$baseUrl/matches/results?page=$page" else "$baseUrl/matches/results"
        val doc: Document = createConnection(url).get()
        return parseMatches(doc)
    }

    /**
     * Retrieves list of all events/tournaments
     *
     * @return List of events
     */
    fun getEvents(): List<Event> {
        val doc: Document = createConnection("$baseUrl/events").get()
        val events = mutableListOf<Event>()
        
        val items = doc.select("a.event-item")
        for (item in items) {
             val url = baseUrl + item.attr("href")
             val eventId = Regex("/event/(\\d+)").find(item.attr("href"))?.groupValues?.getOrNull(1) ?: ""
             val name = item.select(".event-item-title").text()
             
             // Extract status from class like "event-item-desc-item-status mod-ongoing"
             val statusElem = item.select("[class*=event-item-desc-item-status]").first()
             val statusClass = statusElem?.className() ?: ""
             val status = when {
                 statusClass.contains("mod-ongoing") -> "Ongoing"
                 statusClass.contains("mod-completed") -> "Completed"
                 statusClass.contains("mod-upcoming") -> "Upcoming"
                 else -> statusElem?.text() ?: ""
             }
             
             // Extract dates
             val dates = item.select(".event-item-desc-item.mod-dates").text()
             
             // Extract prize pool - get the first part before "Prize Pool"
             val prizeText = item.select(".event-item-desc-item.mod-prize").text()
             val prizePool = prizeText.substringBefore(" Prize Pool").trim()
             
             // Extract region code from flag class (e.g., "flag mod-us" -> "us")
             val locationDiv = item.select(".event-item-desc-item.mod-location").first()
             val flagClass = locationDiv?.select(".flag")?.attr("class") ?: ""
             val region = extractCountryCode(flagClass) ?: ""
             
             // Extract event image
             val imageSrc = item.select(".event-item-thumb img").attr("src")
             val image = if (imageSrc.isNotEmpty()) {
                 if (imageSrc.startsWith("//")) "https:$imageSrc" else imageSrc
             } else null
             
             events.add(
                Event(
                    id = eventId,
                    name = name,
                    status = status,
                    dates = dates,
                    region = region,
                    prizePool = prizePool,
                    image = image,
                    url = url
                )
            )
        }
        return events
    }
    

    
    /**
     * Retrieves player statistics
     *
     * @return List of player statistics
     */
    fun getStats(): List<PlayerStat> {
        val doc: Document = createConnection("$baseUrl/stats").get()
        val stats = mutableListOf<PlayerStat>()
        
        val rows = doc.select("tbody tr")
        for (row in rows) {
             val playerElem = row.select(".mod-player a")
             if (playerElem.isEmpty()) continue
             
             // Extract player name (without team suffix)
             val fullText = playerElem.text()
             val teamName = row.select(".mod-player div").last()?.text() ?: "Unknown"
             
             // Remove team name from player name if it's included
             val player = if (fullText.endsWith(" $teamName")) {
                 fullText.substringBeforeLast(" $teamName")
             } else {
                 fullText
             }

             // Extract agents from image URLs
             val agents = mutableListOf<String>()
             val agentImgs = row.select(".mod-agents img")
             
             for (img in agentImgs) {
                 val src = img.attr("src")
                 if (src.contains("/img/vlr/game/agents/")) {
                     // Extract agent name from URL like "/img/vlr/game/agents/fade.png" -> "fade"
                     val agentName = src.substringAfterLast("/").substringBeforeLast(".")
                     agents.add(agentName)
                 }
             }
             
             // Check for (+N) indicator
             val agentText = row.select(".mod-agents").text()
             val plusMatch = Regex("\\(\\+(\\d+)\\)").find(agentText)
             if (plusMatch != null) {
                 agents.add("+${plusMatch.groupValues[1]}")
             }
             
             val cols = row.select("td")
             
             val rnd = cols.getOrNull(2)?.text() ?: "0"
             val ratingVal = cols.getOrNull(3)?.text() ?: "0"
             val acsVal = cols.getOrNull(4)?.text() ?: "0"
             val kd = cols.getOrNull(5)?.text() ?: "0"
             val kast = cols.getOrNull(6)?.text() ?: "0%"
             val adr = cols.getOrNull(7)?.text() ?: "0"
             val kpr = cols.getOrNull(8)?.text() ?: "0"
             val apr = cols.getOrNull(9)?.text() ?: "0"
             val fkpr = cols.getOrNull(10)?.text() ?: "0"
             val fdpr = cols.getOrNull(11)?.text() ?: "0"
             val hs = cols.getOrNull(12)?.text() ?: "0%"
             
             // Additional stats
             val clPercentage = cols.getOrNull(13)?.text() ?: "0%"
             val clutches = cols.getOrNull(14)?.text() ?: "0"
             val maxKills = cols.getOrNull(15)?.text() ?: "0"
             val kills = cols.getOrNull(16)?.text() ?: "0"
             val deaths = cols.getOrNull(17)?.text() ?: "0"
             val assists = cols.getOrNull(18)?.text() ?: "0"
             val firstKills = cols.getOrNull(19)?.text() ?: "0"
             val firstDeaths = cols.getOrNull(20)?.text() ?: "0"
             
             stats.add(PlayerStat(player, teamName, agents, rnd, ratingVal, acsVal, kd, kast, adr, 
                                 kpr, apr, fkpr, fdpr, hs, clPercentage, clutches, maxKills, 
                                 kills, deaths, assists, firstKills, firstDeaths))
        }
        
        return stats
    }
    
    /**
     * Retrieves news articles
     *
     * @param page Page number to fetch (default: 1)
     * @return List of news items
     */
    fun getNews(page: Int = 1): List<NewsItem> {
        val url = if (page > 1) "$baseUrl/news?page=$page" else "$baseUrl/news"
        val doc: Document = createConnection(url).get()
        val newsList = mutableListOf<NewsItem>()
        
        val items = doc.select("a").toList().filter { 
            it.attr("href").matches(Regex("/\\d+/.*")) && 
            !it.attr("href").contains("/match/") &&
            !it.attr("href").contains("/team/") &&
            it.text().contains("•")
        }

        for (item in items) {
            val newsUrl = baseUrl + item.attr("href")
            val fullText = item.text()
            val parts = fullText.split("•").map { it.trim() }
            
            if (parts.size >= 3) {
                val author = parts.last().replace("by ", "")
                val date = parts[parts.size - 2]
                val content = parts.dropLast(2).joinToString(" • ")
                val title = content 
                val description = "" 
                
                newsList.add(NewsItem(title, description, author, date, newsUrl))
            }
        }
        
        return newsList
    }

    /**
     * Retrieves player transfers
     *
     * @param page Page number to fetch (default: 1)
     * @return List of player transfers
     */
    fun getTransfers(page: Int = 1): List<Transfer> {
        val url = if (page > 1) "$baseUrl/transfers?page=$page" else "$baseUrl/transfers"
        val doc: Document = createConnection(url).get()
        val transfers = mutableListOf<Transfer>()
        
        val potentialRows = doc.select("div").filter { div ->
            div.select("a[href*='/player/']").isNotEmpty() &&
            div.select("a[href*='/team/']").isNotEmpty()
        }
        
        val addedUrls = mutableSetOf<String>()

        for (row in potentialRows) {
            val playerLink = row.select("a[href*='/player/']").first()
            if (playerLink != null) {
                val playerUrl = baseUrl + playerLink.attr("href")
                
                if (addedUrls.contains(playerUrl)) continue
                
                val player = playerLink.text()
                val date = "Recent" 
                
                val teamLinks = row.select("a[href*='/team/']")
                val fromTeam = teamLinks.getOrNull(0)?.text() ?: "Free Agent"
                val toTeam = teamLinks.getOrNull(1)?.text() ?: "Free Agent"
                
                if (row.text().length < 500) { 
                    transfers.add(Transfer(player, fromTeam, toTeam, date, playerUrl))
                    addedUrls.add(playerUrl)
                }
            }
        }
        
        return transfers
    }

    private fun parseMatches(doc: Document): List<Match> {
        val matches = mutableListOf<Match>()
        
        val items = doc.select(".match-item")
        for (item in items) {
            val url = baseUrl + item.attr("href")
            val time = item.select(".match-item-time").text()
            val event = item.select(".match-item-event").text()
            val team1 = item.select(".match-item-vs-team-name").getOrNull(0)?.text() ?: "Unknown"
            val team2 = item.select(".match-item-vs-team-name").getOrNull(1)?.text() ?: "Unknown"
            
            // Extract scores - for completed matches, scores are in .match-item-vs-team-score
            val scoreElements = item.select(".match-item-vs-team-score")
            val score1 = scoreElements.getOrNull(0)?.text()?.trim()
            val score2 = scoreElements.getOrNull(1)?.text()?.trim()

            // Extract Flags
            val flags = item.select(".match-item-vs-team .flag")
            val team1FlagClass = flags.getOrNull(0)?.className() ?: ""
            val team2FlagClass = flags.getOrNull(1)?.className() ?: ""
            
            val team1CountryCode = extractCountryCode(team1FlagClass)
            val team2CountryCode = extractCountryCode(team2FlagClass)
            
            val team1CountryFlag = if (team1CountryCode != null) "https://www.vlr.gg/img/icons/flags/16/$team1CountryCode.png" else null
            val team2CountryFlag = if (team2CountryCode != null) "https://www.vlr.gg/img/icons/flags/16/$team2CountryCode.png" else null

            val eventIcon = extractEventIcon(item)

            matches.add(Match(
                team1 = team1, 
                team2 = team2, 
                time = time, 
                event = event, 
                score1 = score1, 
                score2 = score2, 
                url = url,
                team1CountryFlag = team1CountryFlag,
                team2CountryFlag = team2CountryFlag,
                eventIcon = eventIcon
            ))
        }
        
        return matches
    }
    
    /**
     * Retrieves detailed information about a team
     *
     * @param teamId Team ID
     * @return Team details including roster
     */
    fun getTeam(teamId: String): Team {
        val url = "$baseUrl/team/$teamId"
        val doc: Document = createConnection(url).get()
        
        // Extract team header information
        val teamName = doc.select(".team-header-name h1, h1.wf-title").text()
        val teamTag = doc.select(".team-header-tag").text().ifEmpty { null }
        val logoUrl = doc.select(".team-header-logo img").attr("src").let {
            if (it.startsWith("//")) "https:$it" else it
        }.ifEmpty { null }
        val region = doc.select(".team-header-country").text().ifEmpty { null }
        
        // Extract social links
        val socialLinks = mutableMapOf<String, String>()
        doc.select(".team-header-links a").forEach { link ->
            val href = link.attr("href")
            when {
                href.contains("twitter.com") || href.contains("x.com") -> socialLinks["twitter"] = href
                href.contains("instagram.com") -> socialLinks["instagram"] = href
                href.contains("youtube.com") -> socialLinks["youtube"] = href
                href.contains("twitch.tv") -> socialLinks["twitch"] = href
                href.contains("facebook.com") -> socialLinks["facebook"] = href
                else -> socialLinks["website"] = href
            }
        }
        
        // Extract total winnings - find the label "Total Winnings" and get its sibling
        val totalWinnings = doc.select(".wf-module-label").find { 
            it.text().trim() == "Total Winnings" 
        }?.nextElementSibling()?.text()?.ifEmpty { null }
        
        // Parse roster
        val roster = mutableListOf<TeamPlayer>()
        val rosterElements = doc.select("a[href^='/player/']").toList().filter { 
            it.closest(".wf-card") != null 
        }
        
        for (element in rosterElements) {
            val playerUrl = baseUrl + element.attr("href")
            val alias = element.select(".team-roster-item-name-alias").text()
            val realName = element.select(".team-roster-item-name-real").text().ifEmpty { null }
            val imageUrl = element.select(".team-roster-item-img img").attr("src").let {
                if (it.startsWith("//")) "https:$it" else it
            }.ifEmpty { null }
            
            // Extract player roles/status tags (SUB, INACTIVE, COACH, etc.)
            val roles = element.select(".team-roster-item-name-role").map { it.text().trim() }
            
            if (alias.isNotEmpty()) {
                roster.add(TeamPlayer(alias, realName, playerUrl, imageUrl, roles))
            }
        }
        
        // Parse matches (both recent and upcoming)
        val allMatches = mutableListOf<TeamMatch>()
        doc.select("a.m-item").forEach { matchElement ->
            // Event name is in a div with font-weight: 700 style
            val eventName = matchElement.select(".m-item-event div[style*=font-weight]").text().ifEmpty {
                matchElement.select(".m-item-event .text-of").text()
            }
            
            // Event stage is the remaining text in .m-item-event after the event name
            val eventStage = matchElement.select(".m-item-event").text()
                .replace(eventName, "").trim().ifEmpty { null }
            
            // Opponent is the team name that's NOT in the left position (mod-left)
            // The right team is in .m-item-team.mod-right
            val opponent = matchElement.select(".m-item-team.mod-right").text().trim()
            
            // Score - get both span elements in .m-item-result and join with ":" 
            val scoreSpans = matchElement.select(".m-item-result span")
            val score = if (scoreSpans.size >= 2) {
                "${scoreSpans[0].text()}:${scoreSpans[1].text()}"
            } else {
                matchElement.select(".m-item-result").text().replace("\n", "").trim().ifEmpty { null }
            }
            
            // Date is in the first div child of .m-item-date
            val date = matchElement.select(".m-item-date div").first()?.text()?.trim() ?: 
                       matchElement.select(".m-item-date").text().replace(Regex("\\s+"), " ").trim()
            val matchUrl = baseUrl + matchElement.attr("href")
            
            // Match is upcoming if score doesn't contain a colon (upcoming shows countdown like "53m", recent shows "1:2")
            val isUpcoming = score != null && !score.contains(":")
            
            if (eventName.isNotEmpty() && opponent.isNotEmpty()) {
                allMatches.add(TeamMatch(eventName, eventStage, opponent, score, date, matchUrl, isUpcoming))
            }
        }
        
        // Separate into recent and upcoming
        val recentMatches = allMatches.filter { !it.isUpcoming }
        val upcomingMatches = allMatches.filter { it.isUpcoming }
        
        // Parse event placements
        val eventPlacements = mutableListOf<EventPlacement>()
        doc.select("a.team-event-item").forEach { eventElement ->
            // Event name is in .text-of class
            val eventName = eventElement.select(".text-of").text()
            // Year/date is in the second child div
            val year = eventElement.children().getOrNull(1)?.text() ?: ""
            val eventUrl = baseUrl + eventElement.attr("href")
            
            // Parse results - look for divs with style="margin-top: 5px"
            val results = mutableListOf<PlacementResult>()
            eventElement.select("div[style*=margin-top]").forEach { resultDiv ->
                // Position is in span.team-event-item-series
                val positionSpan = resultDiv.select("span.team-event-item-series").first()
                if (positionSpan != null) {
                    val positionText = positionSpan.text().trim()
                    
                    // Prize is in another span (if present)
                    val allSpans = resultDiv.select("span")
                    val prizeSpan = allSpans.find { it.text().contains("$") }
                    val prize = prizeSpan?.text()?.trim()
                    
                    // Parse position text which may be "Stage – Rank" or just "Rank"
                    val parts = positionText.split("–", "-").map { it.trim() }
                    
                    if (parts.size >= 2) {
                        // Has stage and rank
                        val stage = parts[0]
                        val rank = parts[1]
                        results.add(PlacementResult(stage, rank, prize))
                    } else if (parts.size == 1 && parts[0].isNotEmpty()) {
                        // Just rank, no stage
                        results.add(PlacementResult("", parts[0], prize))
                    }
                }
            }
            
            if (eventName.isNotEmpty() && results.isNotEmpty()) {
                eventPlacements.add(EventPlacement(eventName, year, results, eventUrl))
            }
        }
        
        // Parse rating history
        val ratingHistory = mutableListOf<RatingHistoryEntry>()
        val allRankingLinks = doc.select("a[href*='/rankings/']")
        
        for (link in allRankingLinks) {
            // Rank is inside a div with class "rank-num"
            val rankText = link.select(".rank-num").text().trim()
            // Region is inside a div with class "rating-txt"
            val region = link.select(".rating-txt").text().trim()
            val rankUrl = baseUrl + link.attr("href")
            
            if (rankText.isNotEmpty() && region.isNotEmpty()) {
                ratingHistory.add(RatingHistoryEntry(rankText, region, rankUrl))
            }
        }
        
        // Parse detailed rating history (form rating and ranking history)
        val formRating = mutableListOf<FormRating>()
        val rankingHistory = mutableListOf<RankingHistoryEntry>()
        
        // Iterate through all team core blocks to capture history for different rosters
        doc.select(".team-core-block").forEach { coreBlock ->
            val coreId = coreBlock.attr("data-core-id")
            
            val tips = coreBlock.select("div.tip")
            for (tip in tips) {
                if (tip.hasAttr("data-pt-id")) {
                    // Date is in the first div
                    val date = if (tip.childrenSize() > 0) tip.child(0).text().trim() else ""
                    
                    // Opponent is in .tip-title (or used for Rank in normalization)
                    val opponentTitle = tip.selectFirst(".tip-title")
                    val opponent = opponentTitle?.text()?.replace("vs.", "")?.trim() ?: ""
                    
                    // Event is usually the div between title and result
                    var event = ""
                    val eventDiv = tip.children().find { 
                        it != tip.child(0) && 
                        !it.hasClass("tip-title") && 
                        !it.hasClass("result")
                    }
                    event = eventDiv?.text()?.trim() ?: ""
                    
                    // Result and scores
                    val resultDiv = tip.selectFirst(".result")
                    val resultSpan = resultDiv?.selectFirst("span")
                    val resultStatus = resultSpan?.text()?.trim() ?: "" // "Win" or "Loss"
                    
                    var currentRating = 0
                    var opponentRating = 0
                    
                    if (resultDiv != null) {
                        val scoreText = resultDiv.ownText()
                        val numbers = Regex("\\d+").findAll(scoreText).map { it.value.toInt() }.toList()
                        if (numbers.size >= 2) {
                            currentRating = numbers[0]
                            opponentRating = numbers[1]
                        }
                    }
                    
                    if (date.isNotEmpty()) {
                        // Check if it's a rank update
                        val isRankUpdate = resultStatus.isEmpty() && opponent.startsWith("#")
                        
                        if (isRankUpdate) {
                            // Mapping for rank updates:
                            // Date field holds Region (e.g. "North America")
                            // Event field holds Date (e.g. "June 2020")
                            // Opponent field holds Rank (e.g. "#12")
                            
                            val actualDate = event
                            val rank = opponent
                            
                            rankingHistory.add(RankingHistoryEntry(actualDate, "Rank Update", rank, coreId))
                        } else {
                            // Standard match update
                            formRating.add(FormRating(date, opponent, event, resultStatus, currentRating, opponentRating, coreId))
                        }
                    }
                }
            }
        }
        
        return Team(
            id = teamId,
            name = teamName,
            tag = teamTag,
            logoUrl = logoUrl,
            region = region,
            socialLinks = socialLinks,
            totalWinnings = totalWinnings,
            roster = roster,
            recentMatches = recentMatches,
            upcomingMatches = upcomingMatches,
            eventPlacements = eventPlacements,
            ratingHistory = ratingHistory,
            formRating = formRating,
            rankingHistory = rankingHistory,
            url = url
        )
    }
    
    /**
     * Retrieves detailed information about a player
     *
     * @param playerId Player ID
     * @return Player details including past teams and agents
     */
    fun getPlayer(playerId: String): Player {
        val url = "$baseUrl/player/$playerId"
        val doc: Document = createConnection(url).get()
        
        val playerName = doc.select(".wf-title").text()
        val realName = doc.select(".player-header-name").text()
        
        // Extract country
        val countryFlag = doc.select(".player-header-country .flag").attr("class").let { classStr ->
            val match = Regex("mod-([a-z]+)").find(classStr)
            val code = match?.groupValues?.get(1)
            if (code != null) "https://www.vlr.gg/img/icons/flags/16/$code.png" else null
        }
        val country = doc.select(".player-header-country").text()
        
        // Current team
        val currentTeam = doc.select(".wf-module-item a[href*='/team/']").first()?.text()
        
        // Agents (from recent matches or stats)
        val agents = doc.select(".mod-agents img").map { it.attr("title") }.distinct()
        
        // Past teams
        val pastTeams = mutableListOf<PastTeam>()
        val pastTeamElements = doc.select(".player-summary-container a[href*='/team/']")
        
        for (element in pastTeamElements) {
            val teamUrl = baseUrl + element.attr("href")
            val teamName = element.text()
            val period = element.parent()?.text()?.replace(teamName, "")?.trim()
            
            if (teamName.isNotEmpty() && teamName != currentTeam) {
                pastTeams.add(PastTeam(teamName, period, teamUrl))
            }
        }
        
        return Player(playerId, playerName, realName.ifEmpty { null }, country.ifEmpty { null }, 
                     countryFlag, currentTeam, agents, pastTeams.distinctBy { it.name }, url)
    }
    
    /**
     * Retrieves currently live matches
     *
     * @return List of live matches
     */
    fun getLiveMatches(): List<LiveMatch> {
        val doc: Document = createConnection(baseUrl).get()
        val liveMatches = mutableListOf<LiveMatch>()
        
        // Look for live match containers - new structure uses .wf-card.hz-match
        // We filter for ones that are actually live (usually have a timestamp or "LIVE" text, 
        // but typically the homepage top section contains live/upcoming. Live ones often have score or specific status)
        // Based on debug output, live matches have "mod-bg-after-green" or "mod-bg-after-orange" and contain "hz-match-team-score"
        
        val matchCards = doc.select(".wf-card.hz-match")
        
        for (item in matchCards) {
             // Check if it's live/ongoing. The debug output showed live matches. 
             // Usually live matches have a score that is not empty.
             val scoreElements = item.select(".hz-match-team-score")
             val score1 = scoreElements.getOrNull(0)?.text()?.trim() ?: ""
             val score2 = scoreElements.getOrNull(1)?.text()?.trim() ?: ""
             
             // If scores are empty or just "-", it might be upcoming. 
             // However, the user specifically asked for /live.
             // In the old scraper, we looked for .mod-live.
             // If VLR changed the structure, we might need to rely on the "LIVE" tag or similar.
             // Debugging showed: <div class="h-match-eta mod-live"> inside specific containers? 
             // No, the debug showed .h-match-eta.mod-live elements, but their parents were different.
             // Let's rely on the existence of scores for now or if we can find a "LIVE" indicator.
             // Actually, the debug candidate showed: <a class="wf-card hz-match mod-bg-after-green" ...>
             // "mod-bg-after-green" suggests live/active.
             
             // Let's stick to parsing all hz-match cards and filtering for those that look live 
             // (have scores or specific class).
             
             val matchUrl = normalizeUrl(item.attr("href"))
             val event = item.select(".hz-match-event").text().trim()
             val status = item.select(".hz-match-series").text().trim()
             
             // Teams
             val teams = item.select(".hz-match-team-name")
             val team1 = teams.getOrNull(0)?.text()?.trim() ?: "Unknown"
             val team2 = teams.getOrNull(1)?.text()?.trim() ?: "Unknown"
             
             // Flags
             val team1FlagClass = teams.getOrNull(0)?.select("i.flag")?.attr("class") ?: ""
             val team2FlagClass = teams.getOrNull(1)?.select("i.flag")?.attr("class") ?: ""
             
             val team1CountryCode = extractCountryCode(team1FlagClass)
             val team2CountryCode = extractCountryCode(team2FlagClass)
             
             val team1CountryFlag = if (team1CountryCode != null) "https://www.vlr.gg/img/icons/flags/16/$team1CountryCode.png" else null
             val team2CountryFlag = if (team2CountryCode != null) "https://www.vlr.gg/img/icons/flags/16/$team2CountryCode.png" else null
             
             val eventIcon = null // Not easily available in new card structure
             
             // Only add if it seems to be a valid match card
             if (team1 != "Unknown" && team2 != "Unknown") {
                 liveMatches.add(LiveMatch(team1, team2, score1, score2, event, status, 
                                          team1CountryFlag, team2CountryFlag, eventIcon, matchUrl))
             }
        }
        
        return liveMatches
    }
    
    /**
     * Retrieves detailed information about an event
     *
     * @param eventId Event ID
     * @return Event details including participating teams
     */
    fun getEventDetail(eventId: String): EventDetail {
        return getEventDetailByUrl("$baseUrl/event/$eventId")
    }

    /**
     * Retrieves detailed information about an event using a full VLR event URL
     *
     * @param eventUrl Full event URL (e.g. https://www.vlr.gg/event/2684/vct-2026-emea-kickoff)
     * @return Event details including participating teams
     */
    fun getEventDetailByUrl(eventUrl: String): EventDetail {
        val normalizedUrl = normalizeUrl(eventUrl)
        val doc: Document = createConnection(normalizedUrl).get()
        val eventId = Regex("/event/(\\d+)").find(normalizedUrl)?.groupValues?.getOrNull(1)
            ?: Regex("/event/(\\d+)").find(doc.location())?.groupValues?.getOrNull(1)
            ?: "unknown"

        val eventName = doc.selectFirst("h1.wf-title, h1")?.text()?.trim().orEmpty()

        val rawDates = firstNonEmptyText(
            doc,
            ".event-desc-item.mod-dates",
            ".event-header-subtitle"
        ) ?: extractLabeledValue(doc, "Dates").orEmpty()
        val dates = rawDates.replace("\\s+".toRegex(), " ").trim()

        val rawPrize = firstNonEmptyText(
            doc,
            ".event-desc-item.mod-prize",
            ".event-prize"
        ) ?: extractLabeledValue(doc, "Prize")
        val prizePool = rawPrize
            ?.replace("Prize Pool", "", ignoreCase = true)
            ?.replace("\\s+".toRegex(), " ")
            ?.trim()
            ?.ifEmpty { null }

        val location = (firstNonEmptyText(
            doc,
            ".event-desc-item.mod-location",
            ".event-header-location"
        ) ?: extractLabeledValue(doc, "Location"))
            ?.replace("\\s+".toRegex(), " ")
            ?.trim()
            ?.ifEmpty { null }

        val format = extractLabeledValue(doc, "Format")
            ?.replace("\\s+".toRegex(), " ")
            ?.trim()
            ?.ifEmpty { null }

        // Extract event logo/image
        val logoSrc = doc.select(".event-header-thumb img").attr("src")
        val logoUrl = when {
            logoSrc.isEmpty() -> null
            logoSrc.startsWith("//") -> "https:$logoSrc"
            logoSrc.startsWith("/") -> "$baseUrl$logoSrc"
            else -> logoSrc
        }

        // Parse participating teams: prefer .event-teams-container cards (have logos), then other team links.
        val teamsByUrl = linkedMapOf<String, EventTeam>()
        val teamCards = doc.select(".event-teams-container .event-team")
        for (card in teamCards) {
            val teamLink = card.select("a.event-team-name[href^='/team/']").first() ?: continue
            val href = teamLink.attr("href")
            val teamUrl = normalizeUrl(href)
            val teamName = teamLink.text().replace("\\s+".toRegex(), " ").trim()
            if (teamName.isBlank()) continue
            val teamLogoSrc = card.select("img.event-team-players-mask-team").attr("src")
                .ifEmpty { card.select(".event-team-players-mask img").attr("src") }
            val teamLogoUrl = when {
                teamLogoSrc.isEmpty() -> null
                teamLogoSrc.startsWith("//") -> "https:$teamLogoSrc"
                teamLogoSrc.startsWith("/") -> "$baseUrl$teamLogoSrc"
                else -> teamLogoSrc
            }
            teamsByUrl[teamUrl] = EventTeam(
                name = teamName,
                tag = null,
                standing = null,
                logoUrl = teamLogoUrl,
                url = teamUrl
            )
        }
        // Backfill any team links elsewhere on the page (e.g. prize distribution) without overwriting existing.
        val teamLinks = doc.select("a[href^='/team/']")
        for (teamLink in teamLinks) {
            val href = teamLink.attr("href")
            val teamUrl = normalizeUrl(href)
            if (teamsByUrl.containsKey(teamUrl)) continue
            val teamName = firstNonEmpty(
                teamLink.select(".event-team-name, .text-of, .ge-text").text(),
                teamLink.ownText(),
                teamLink.text()
            )?.replace("\\s+".toRegex(), " ")?.trim() ?: continue
            if (teamName.length > 80) continue
            val teamCard = teamLink.closest(".event-team")
            val teamLogoSrc = teamCard?.select("img.event-team-players-mask-team, .event-team-players-mask img")?.firstOrNull()?.attr("src")
                ?: teamLink.select("img").attr("src")
            val teamLogoUrl = when {
                teamLogoSrc.isNullOrEmpty() -> null
                teamLogoSrc.startsWith("//") -> "https:$teamLogoSrc"
                teamLogoSrc.startsWith("/") -> "$baseUrl$teamLogoSrc"
                else -> teamLogoSrc
            }
            val teamTag = teamLink.select(".event-team-tag").text().trim().ifEmpty { null }
            val standing = teamLink.closest(".event-team-item")?.select(".event-team-rank")?.text()?.trim()?.ifEmpty { null }
            teamsByUrl[teamUrl] = EventTeam(
                name = teamName,
                tag = teamTag,
                standing = standing,
                logoUrl = teamLogoUrl,
                url = teamUrl
            )
        }

        return EventDetail(
            id = eventId,
            name = eventName,
            dates = dates,
            prizePool = prizePool,
            teams = teamsByUrl.values.toList(),
            brackets = parseEventBrackets(doc),
            location = location,
            format = format,
            logoUrl = logoUrl,
            url = normalizedUrl
        )
    }

    private fun parseEventBrackets(doc: Document): List<EventBracketGroup> {
        val groups = mutableListOf<EventBracketGroup>()
        val bracketContainers = doc.select(".event-brackets-container .bracket-container")

        for (container in bracketContainers) {
            val classBasedGroupName = when {
                container.classNames().contains("mod-upper") -> "Upper"
                container.classNames().contains("mod-lower") -> "Lower"
                container.classNames().contains("mod-middle") -> "Middle"
                else -> "Bracket"
            }

            val rounds = mutableListOf<EventBracketRound>()
            val columns = container.select(".bracket-col")
            for (column in columns) {
                val roundName = column.select(".bracket-col-label").firstOrNull()?.text().orEmpty()
                    .replace("\\s+".toRegex(), " ")
                    .trim()
                if (roundName.isEmpty()) continue

                val matches = mutableListOf<EventBracketMatch>()
                val matchItems = column.select(".bracket-row > a.bracket-item")
                for (matchItem in matchItems) {
                    val matchUrl = matchItem.attr("href").takeIf { it.isNotBlank() }?.let { normalizeUrl(it) }
                    val teamNodes = matchItem.select(".bracket-item-team")

                    val firstTeamNode = teamNodes.getOrNull(0)
                    val secondTeamNode = teamNodes.getOrNull(1)

                    val team1 = parseBracketTeam(firstTeamNode)
                    val team2 = parseBracketTeam(secondTeamNode)
                    val status = matchItem.select(".bracket-item-status").text()
                        .replace("\\s+".toRegex(), " ")
                        .trim()
                        .ifEmpty { null }

                    matches.add(EventBracketMatch(team1 = team1, team2 = team2, status = status, url = matchUrl))
                }

                if (matches.isNotEmpty()) {
                    rounds.add(EventBracketRound(name = roundName, matches = matches))
                }
            }

            if (rounds.isNotEmpty()) {
                val inferredGroupName = inferBracketGroupName(rounds.first().name)
                groups.add(EventBracketGroup(name = inferredGroupName ?: classBasedGroupName, rounds = rounds))
            }
        }

        return groups
    }

    private fun inferBracketGroupName(roundName: String): String? {
        val normalized = roundName.replace("\\s+".toRegex(), " ").trim()
        if (normalized.isEmpty()) return null

        val roundPrefix = Regex("^([A-Za-z]+)\\s+Round\\b", RegexOption.IGNORE_CASE)
            .find(normalized)
            ?.groupValues
            ?.getOrNull(1)
        if (!roundPrefix.isNullOrBlank()) {
            return roundPrefix.replaceFirstChar { it.uppercase() }
        }

        return when {
            normalized.contains("Upper", ignoreCase = true) -> "Upper"
            normalized.contains("Lower", ignoreCase = true) -> "Lower"
            normalized.contains("Middle", ignoreCase = true) -> "Middle"
            else -> null
        }
    }

    private fun parseBracketTeam(teamNode: Element?): EventBracketTeam {
        if (teamNode == null) {
            return EventBracketTeam(name = "TBD", score = null, isWinner = false, logoUrl = null)
        }

        val name = firstNonEmpty(
            teamNode.select(".bracket-item-team-name span").text(),
            teamNode.select(".bracket-item-team-name").text(),
            teamNode.text()
        )?.replace("\\s+".toRegex(), " ")?.trim().orEmpty().ifEmpty { "TBD" }

        val score = teamNode.select(".bracket-item-team-score").text().trim().ifEmpty { null }
        val isWinner = teamNode.classNames().contains("mod-winner")

        val logoSrc = teamNode.select(".bracket-item-team-name img").attr("src")
        val logoUrl = when {
            logoSrc.isEmpty() -> null
            logoSrc.startsWith("//") -> "https:$logoSrc"
            logoSrc.startsWith("/") -> "$baseUrl$logoSrc"
            else -> logoSrc
        }

        return EventBracketTeam(
            name = name,
            score = score,
            isWinner = isWinner,
            logoUrl = logoUrl
        )
    }

    private fun firstNonEmptyText(doc: Document, vararg selectors: String): String? {
        for (selector in selectors) {
            val value = doc.select(selector).text().trim()
            if (value.isNotEmpty()) return value
        }
        return null
    }

    private fun extractLabeledValue(doc: Document, label: String): String? {
        val labelElement = doc.select("div, span, td, th, strong")
            .firstOrNull { it.text().trim().equals(label, ignoreCase = true) }
            ?: return null

        val fromNextSibling = labelElement.nextElementSibling()?.text()?.trim()
        if (!fromNextSibling.isNullOrEmpty()) return fromNextSibling

        val parentText = labelElement.parent()?.text()?.trim().orEmpty()
        if (parentText.equals(label, ignoreCase = true)) return null

        return parentText.removePrefix(label).replace("\\s+".toRegex(), " ").trim().ifEmpty { null }
    }

    private fun firstNonEmpty(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }
    }

    private fun normalizeUrl(urlOrPath: String): String {
        if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) return urlOrPath
        return if (urlOrPath.startsWith("/")) "$baseUrl$urlOrPath" else "$baseUrl/$urlOrPath"
    }
    
    /**
     * Searches for teams by query string
     *
     * @param query Search query
     * @return List of matching team search results
     */
    fun searchTeams(query: String): List<TeamSearchResult> {
        val url = "$baseUrl/search/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val doc: Document = createConnection(url).get()
        val results = mutableListOf<TeamSearchResult>()
        
        val teamElements = doc.select("a[href*='/team/']")
        
        for (element in teamElements) {
            val teamUrl = baseUrl + element.attr("href")
            val teamId = element.attr("href").split("/").getOrNull(2) ?: continue
            val teamName = element.select(".search-item-title, .team-name").text()
            val teamTag = element.select(".search-item-desc, .team-tag").text()
            val region = element.select(".search-item-region").text()
            val logo = element.select("img").attr("src").let {
                if (it.startsWith("//")) "https:$it" else it
            }
            
            if (teamName.isNotEmpty()) {
                results.add(TeamSearchResult(teamId, teamName, teamTag, region, logo.ifEmpty { null }, teamUrl))
            }
        }
        
        return results.distinctBy { it.id }
    }
    
    /**
     * Searches for players by query string
     *
     * @param query Search query
     * @return List of matching player search results
     */
    fun searchPlayers(query: String): List<PlayerSearchResult> {
        val url = "$baseUrl/search/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val doc: Document = createConnection(url).get()
        val results = mutableListOf<PlayerSearchResult>()
        
        val playerElements = doc.select("a[href*='/player/']")
        
        for (element in playerElements) {
            val playerUrl = baseUrl + element.attr("href")
            val playerId = element.attr("href").split("/").getOrNull(2) ?: continue
            val playerName = element.select(".search-item-title, .player-name").text()
            val realName = element.select(".search-item-desc, .player-real-name").text()
            val team = element.select(".search-item-team").text()
            val country = element.select(".search-item-country").text()
            
            if (playerName.isNotEmpty()) {
                results.add(PlayerSearchResult(playerId, playerName, realName.ifEmpty { null }, 
                                              team.ifEmpty { null }, country.ifEmpty { null }, playerUrl))
            }
        }
        
        return results.distinctBy { it.id }
    }
    
    /**
     * Retrieves matches filtered by region
     *
     * @param region Region filter
     * @param page Page number (default: 1)
     * @return List of matches in the specified region
     */
    fun getMatchesByRegion(region: String, page: Int = 1): List<Match> {
        val url = if (page > 1) {
            "$baseUrl/matches?region=$region&page=$page"
        } else {
            "$baseUrl/matches?region=$region"
        }
        val doc: Document = createConnection(url).get()
        return parseMatches(doc)
    }
    
    /**
     * Retrieves matches for a specific event
     *
     * @param eventId Event ID
     * @param page Page number (default: 1)
     * @return List of matches in the specified event
     */
    fun getMatchesByEvent(eventId: String, page: Int = 1): List<Match> {
        val url = if (page > 1) {
            "$baseUrl/event/matches/$eventId?page=$page"
        } else {
            "$baseUrl/event/matches/$eventId"
        }
        val doc: Document = createConnection(url).get()
        return parseMatches(doc)
    }
    

    
    /**
     * Retrieves available streams for a match
     *
     * @param matchUrl Match page URL
     * @return List of available stream links
     */
    fun getMatchStreams(matchUrl: String): List<StreamLink> {
        val doc: Document = createConnection(matchUrl).get()
        val streams = mutableListOf<StreamLink>()
        
        val streamElements = doc.select(".match-streams-btn, a[href*='twitch.tv'], a[href*='youtube.com']")
        
        for (element in streamElements) {
            val streamUrl = element.attr("href")
            if (streamUrl.isEmpty()) continue
            
            val platform = when {
                streamUrl.contains("twitch.tv") -> "Twitch"
                streamUrl.contains("youtube.com") || streamUrl.contains("youtu.be") -> "YouTube"
                else -> "Other"
            }
            
            val language = element.select(".flag").attr("title").ifEmpty { null }
            
            streams.add(StreamLink(platform, streamUrl, language))
        }
        
        return streams.distinctBy { it.url }
    }

    fun getGlobalRankings(): List<RegionRankings> {
        val doc = createConnection("$baseUrl/rankings").get()
        return getRegionRankings(doc)
    }

    fun getRankingsByRegion(region: String): List<TeamRanking> {
        val doc = createConnection("$baseUrl/rankings/$region").get()
        return getRegionRankings(doc).flatMap { it.rankings }
    }

    private fun getRegionRankings(doc: Document): List<RegionRankings> {
        val allRegionRankings = mutableListOf<RegionRankings>()

        // Try div-based structure first (individual regional pages like /rankings/north-america)
        val divItems = doc.select("div.rank-item")
        if (divItems.isNotEmpty()) {
            val rankings = mutableListOf<TeamRanking>()
            // Extract region name from page title or assume mostly one region
            var regionName = doc.title().replace("Valorant Rankings: ", "").replace(" | VLR.gg", "").trim()
            if (regionName.isEmpty()) regionName = "Unknown Region"

            for (item in divItems) {
                val rankElem = item.select(".rank-item-rank-num")
                if (rankElem.isEmpty()) continue

                val rank = rankElem.text().trim()
                val teamLink = item.select("a.rank-item-team").first() ?: continue
                
                // Team name extraction
                val teamText = teamLink.attr("data-sort-value").ifEmpty { 
                    teamLink.select(".ge-text").first()?.ownText()?.trim() ?: ""
                }
                val teamRegion = teamLink.select(".rank-item-team-country").text()
                val url = baseUrl + teamLink.attr("href")
                val points = item.select(".rank-item-rating").first()?.text() ?: ""
                
                val logoUrl = teamLink.select("img").attr("src").let {
                    if (it.startsWith("//")) "https:$it" 
                    else if (it.startsWith("/")) "https://www.vlr.gg$it"
                    else it
                }.ifEmpty { null }

                if (teamText.isNotEmpty()) {
                    rankings.add(TeamRanking(rank, teamText, teamRegion, points, url, logoUrl))
                }
            }
            if (rankings.isNotEmpty()) {
                allRegionRankings.add(RegionRankings(regionName, rankings))
            }
        } else {
            // Fall back to table-based structure (World page /rankings)
            // The global page has h2 headers followed by tables
            val headers = doc.select("h2")
            for (header in headers) {
                val regionName = header.text().trim()
                if (regionName.isEmpty()) continue
                
                // Find the next table
                var sibling = header.nextElementSibling()
                while (sibling != null && !sibling.tagName().equals("table", ignoreCase = true)) {
                    sibling = sibling.nextElementSibling()
                }
                
                if (sibling != null && sibling.tagName().equals("table", ignoreCase = true)) {
                    val rankings = mutableListOf<TeamRanking>()
                    val rows = sibling.select("tr.wf-card")
                    for (row in rows) {
                        val rankCell = row.select("td.rank-item-rank").first() ?: continue
                        val rank = rankCell.select("a").text().ifEmpty { rankCell.text() }.trim()
                        
                        val teamCell = row.select("td.rank-item-team").first() ?: continue
                        val teamLink = teamCell.select("a").first() ?: continue
                        
                        val teamDiv = teamLink.select("div").first()
                        val teamText = teamDiv?.ownText()?.trim() ?: ""
                        val region = teamDiv?.select(".rank-item-team-country")?.text() ?: ""
                        val url = baseUrl + teamLink.attr("href")
                        val points = row.select("td.rank-item-rating").text()
                        
                        val logoUrl = teamLink.select("img").attr("src").let {
                            if (it.startsWith("//")) "https:$it" 
                            else if (it.startsWith("/")) "https://www.vlr.gg$it"
                            else it
                        }.ifEmpty { null }

                        if (teamText.isNotEmpty()) {
                            rankings.add(TeamRanking(rank, teamText, region, points, url, logoUrl))
                        }
                    }
                    if (rankings.isNotEmpty()) {
                        allRegionRankings.add(RegionRankings(regionName, rankings))
                    }
                }
            }
            
            // If no headers found but tables exist (maybe only one table?), fallback to generic
            if (allRegionRankings.isEmpty()) {
                 val tables = doc.select("table.wf-faux-table")
                 if (tables.isNotEmpty()) {
                     val rankings = mutableListOf<TeamRanking>()
                     for (table in tables) {
                         val rows = table.select("tr.wf-card")
                         for (row in rows) {
                             // ... parse row ...
                             val rankCell = row.select("td.rank-item-rank").first() ?: continue
                             val rank = rankCell.select("a").text().ifEmpty { rankCell.text() }.trim()
                             
                             val teamCell = row.select("td.rank-item-team").first() ?: continue
                             val teamLink = teamCell.select("a").first() ?: continue
                             
                             val teamDiv = teamLink.select("div").first()
                             val teamText = teamDiv?.ownText()?.trim() ?: ""
                             val region = teamDiv?.select(".rank-item-team-country")?.text() ?: ""
                             val url = baseUrl + teamLink.attr("href")
                             val points = row.select("td.rank-item-rating").text() 
                             
                             val logoUrl = teamLink.select("img").attr("src").let {
                                if (it.startsWith("//")) "https:$it" 
                                else if (it.startsWith("/")) "https://www.vlr.gg$it"
                                else it
                            }.ifEmpty { null }

                             if (teamText.isNotEmpty()) {
                                 rankings.add(TeamRanking(rank, teamText, region, points, url, logoUrl))
                             }
                         }
                     }
                      if (rankings.isNotEmpty()) {
                        allRegionRankings.add(RegionRankings("World", rankings))
                    }
                 }
            }
        }

        return allRegionRankings
    }
    
    /**
     * Retrieves comprehensive match details from a match detail page
     *
     * @param matchUrl Match detail page URL
     * @return Comprehensive match details including teams, scores, maps, streams, and player stats
     */
    fun getMatchDetails(matchUrl: String): MatchDetail {
        val doc: Document = createConnection(matchUrl).get()
        
        // Extract event name, image, and subtitle
        val eventHeader = doc.select("a.match-header-event").first()
        val eventDivs = eventHeader?.select("div") ?: emptyList()
        
        // Extract event image
        val eventImageSrc = eventHeader?.select("img")?.attr("src") ?: ""
        val eventImage = if (eventImageSrc.isNotEmpty()) {
            if (eventImageSrc.startsWith("//")) "https:$eventImageSrc" else eventImageSrc
        } else null
        
        // Extract series info from .match-header-event-series
        val seriesInfo = eventHeader?.select(".match-header-event-series")?.text()?.trim() ?: ""
        
        // Use only series info for subtitle (not the note/status like FINAL, LIVE, etc.)
        val matchSubtitle = seriesInfo
        
        // First div contains the main event name (with font-weight: 700)
        // We select the specific inner div or exclude the subtitle to prevent duplication
        val mainEventName = eventHeader?.selectFirst("div[style*='font-weight: 700']")?.text()?.trim()
            ?: eventHeader?.selectFirst("div > div")?.text()?.trim()
            ?: eventDivs.firstOrNull()?.text()?.replace(seriesInfo, "")?.trim()
            ?: ""
        
        val eventName = mainEventName
        
        // Extract date and time from separate .moment-tz-convert elements
        val dateElem = doc.select("div.match-header-date").first()
        val momentElements = dateElem?.select("div.moment-tz-convert") ?: emptyList()
        val date = momentElements.getOrNull(0)?.text()?.trim() ?: ""
        val time = momentElements.getOrNull(1)?.text()?.trim() ?: ""
        
        // Extract patch info
        val patchText = dateElem?.select("div")?.last()?.text() ?: ""
        val patch = if (patchText.contains("Patch")) patchText else null
        
        // Extract team 1 info
        val team1Link = doc.select("a.match-header-link.mod-1").first()
        val team1Name = team1Link?.select("div.wf-title-med")?.text() ?: "Unknown"
        val team1LogoSrc = team1Link?.select("img")?.attr("src") ?: ""
        val team1Logo = if (team1LogoSrc.isNotEmpty()) {
            if (team1LogoSrc.startsWith("//")) "https:$team1LogoSrc" else team1LogoSrc
        } else null
        val team1Url = team1Link?.attr("href")?.let { 
            if (it.startsWith("/")) "https://www.vlr.gg$it" else it 
        }
        
        // Extract team 2 info
        val team2Link = doc.select("a.match-header-link.mod-2").first()
        val team2Name = team2Link?.select("div.wf-title-med")?.text() ?: "Unknown"
        val team2LogoSrc = team2Link?.select("img")?.attr("src") ?: ""
        val team2Logo = if (team2LogoSrc.isNotEmpty()) {
            if (team2LogoSrc.startsWith("//")) "https:$team2LogoSrc" else team2LogoSrc
        } else null
        val team2Url = team2Link?.attr("href")?.let { 
            if (it.startsWith("/")) "https://www.vlr.gg$it" else it 
        }
        
        // Extract scores from .js-spoiler container
        // Scores are in spans: winner, colon, loser (or just two spans for team1, team2)
        val scoreSpans = doc.select("div.match-header-vs-score .js-spoiler span")
        val team1Score = scoreSpans.getOrNull(0)?.text()?.trim()?.ifEmpty { null }
        val team2Score = scoreSpans.getOrNull(2)?.text()?.trim()?.ifEmpty { null }
        
        // Extract status, format, and time until match
        // All these are in .match-header-vs-note elements, need to identify by content
        val vsNoteElements = doc.select("div.match-header-vs-note")
        var status = "upcoming"
        var format = ""
        var timeUntilMatch: String? = null
        
        for (noteElem in vsNoteElements) {
            val noteText = noteElem.text().trim()
            when {
                noteText.matches(Regex("(?i)Bo\\d+")) -> format = noteText  // Bo3, Bo5, etc.
                noteText.matches(Regex(".*\\d+[hmd].*", RegexOption.IGNORE_CASE)) -> timeUntilMatch = noteText  // 2h 8m, 1d 5h, etc.
                noteText.equals("final", ignoreCase = true) -> status = "completed"
                noteText.equals("live", ignoreCase = true) -> status = "live"
            }
        }
        
        // Extract bans/picks info
        val bansPicksInfo = doc.select("div.match-header-note").text().ifEmpty { null }
        
        // Extract streams
        val streams = mutableListOf<StreamInfo>()
        val streamElements = doc.select("div.match-streams a.match-streams-btn")
        for (streamElem in streamElements) {
            val streamName = streamElem.select("span").text()
            val streamUrl = streamElem.attr("href")
            if (streamName.isNotEmpty() && streamUrl.isNotEmpty()) {
                streams.add(StreamInfo(streamName, streamUrl))
            }
        }
        
        // Extract map results (for completed matches)
        val maps = mutableListOf<MapResult>()
        val mapElements = doc.select("div.vm-stats-game")
        for (mapElem in mapElements) {
            val gameId = mapElem.attr("data-game-id")
            
            // Skip "All Maps" as it doesn't have map-specific header data
            if (gameId == "all") continue
            
            val header = mapElem.select(".vm-stats-game-header").first()
            if (header == null) {
                println("Warning: Map container with game-id=$gameId has no header")
                continue
            }
            
            // Extract map name and duration
            val mapDiv = header.select(".map").first()
            val rawMapName = mapDiv?.text()?.trim() ?: ""
            
            // Parse map name: "Split PICK 51:55" -> mapName: "Split", duration: "51:55"
            // Split by any whitespace (spaces, tabs, newlines)
            val parts = rawMapName.split(Regex("\\s+"))
            val cleanMapName = parts.firstOrNull() ?: ""
            
            // Extract duration from .map-duration div
            val duration = mapDiv?.select(".map-duration")?.text()?.trim()
            
            // Extract team data
            val teamDivs = header.select(".team")
            val team1Div = teamDivs.getOrNull(0)
            val team2Div = teamDivs.getOrNull(1)
            
            // Team 1 scores
            val team1ScoreElem = team1Div?.select(".score")?.first()
            val team1Score = team1ScoreElem?.text()?.trim() ?: ""
            val team1IsWinner = team1ScoreElem?.hasClass("mod-win") == true
            val team1AttackScore = team1Div?.select(".mod-t")?.text()?.trim()
            val team1DefendScore = team1Div?.select(".mod-ct")?.text()?.trim()
            
            // Team 2 scores
            val team2ScoreElem = team2Div?.select(".score")?.first()
            val team2Score = team2ScoreElem?.text()?.trim() ?: ""
            val team2IsWinner = team2ScoreElem?.hasClass("mod-win") == true
            val team2AttackScore = team2Div?.select(".mod-t")?.text()?.trim()
            val team2DefendScore = team2Div?.select(".mod-ct")?.text()?.trim()
            
            // Determine winner
            val winner = when {
                team1IsWinner -> "team1"
                team2IsWinner -> "team2"
                else -> null
            }
            
            // Determine which team picked the map based on "picked mod-1" or "picked mod-2" class
            val pickedBy = when {
                mapElem.select(".picked.mod-1").isNotEmpty() -> "team1"
                mapElem.select(".picked.mod-2").isNotEmpty() -> "team2"
                else -> null
            }
            
            if (cleanMapName.isNotEmpty()) {
                maps.add(MapResult(
                    mapName = cleanMapName,
                    team1Score = TeamScore(team1Score, team1AttackScore, team1DefendScore),
                    team2Score = TeamScore(team2Score, team2AttackScore, team2DefendScore),
                    winner = winner,
                    pickedBy = pickedBy,
                    duration = duration
                ))
            }
        }
        
        // Extract player statistics and rounds organized by map (for completed matches)
        val mapDataList = mutableListOf<MapStatsData>()
        
        // Find all game stat containers
        val gameContainers = doc.select(".vm-stats-game")
        
        for (container in gameContainers) {
            val gameId = container.attr("data-game-id")
            
            // Get map name from the corresponding navigation item
            val navItem = doc.select(".vm-stats-gamesnav-item[data-game-id=\"$gameId\"]").first()
            val mapName = navItem?.text()?.trim() ?: continue
            
            val statsForThisMap = mutableListOf<PlayerMatchStat>()
            
            // Each container has 2 tables (one per team)
            val tables = container.select("table")
            
            for (table in tables) {
                val rows = table.select("tbody tr")
                
                for (row in rows) {
                    val cells = row.select("td")
                    if (cells.size < 14) continue
                    
                    // Extract player name (cell 0)
                    val playerCell = cells.getOrNull(0)
                    val playerName = playerCell?.select("a div")?.first()?.text()?.trim() ?: continue
                    
                    // Extract team name (cell 0, last div)
                    val teamName = playerCell?.select("a div")?.last()?.text()?.trim() ?: ""
                    
                    // Extract agents (cell 1) - multiple agents possible in "All Maps"
                    val agentImgs = cells.getOrNull(1)?.select("img") ?: emptyList()
                    val agents = agentImgs.mapNotNull { img ->
                        val title = img.attr("title")
                        val alt = img.attr("alt")
                        val src = img.attr("src")
                        when {
                            title.isNotEmpty() -> title
                            alt.isNotEmpty() -> alt
                            src.contains("/agents/") -> src.substringAfterLast("/").substringBeforeLast(".")
                            else -> null
                        }
                    }
                    
                    // Helper function to extract SideStat from a cell
                    fun extractSideStat(cell: org.jsoup.nodes.Element?): SideStat {
                        val all = cell?.select("span.mod-both")?.text()?.trim() ?: "0"
                        val attack = cell?.select("span.mod-t")?.text()?.trim() ?: "0"
                        val defend = cell?.select("span.mod-ct")?.text()?.trim() ?: "0"
                        return SideStat(all, attack, defend)
                    }
                    
                    // Extract stats with side breakdown (all/attack/defend)
                    val rating = extractSideStat(cells.getOrNull(2))
                    val acs = extractSideStat(cells.getOrNull(3))
                    val kills = extractSideStat(cells.getOrNull(4))
                    val deaths = extractSideStat(cells.getOrNull(5))
                    val assists = extractSideStat(cells.getOrNull(6))
                    val kdDiff = extractSideStat(cells.getOrNull(7))
                    val kast = extractSideStat(cells.getOrNull(8))
                    val adr = extractSideStat(cells.getOrNull(9))
                    val hs = extractSideStat(cells.getOrNull(10))
                    val fk = extractSideStat(cells.getOrNull(11))
                    val fd = extractSideStat(cells.getOrNull(12))
                    val fkDiff = extractSideStat(cells.getOrNull(13))
                    
                    statsForThisMap.add(PlayerMatchStat(
                        playerName = playerName,
                        team = teamName,
                        agents = agents,
                        rating = rating,
                        acs = acs,
                        kills = kills,
                        deaths = deaths,
                        assists = assists,
                        kdDiff = kdDiff,
                        kast = kast,
                        adr = adr,
                        hs = hs,
                        fk = fk,
                        fd = fd,
                        fkDiff = fkDiff
                    ))
                }
            }
            
            // Extract rounds (skip for "All Maps")
            val rounds = mutableListOf<Round>()
            if (gameId != "all") {
                val roundsContainer = container.select(".vlr-rounds").first()
                if (roundsContainer != null) {
                    val roundCols = roundsContainer.select(".vlr-rounds-row-col[title]")
                    for (roundCol in roundCols) {
                        val roundNum = roundCol.select(".rnd-num").text()
                        val score = roundCol.attr("title")
                        
                        val winSq = roundCol.select(".rnd-sq.mod-win").first()
                        val squares = roundCol.select(".rnd-sq")
                        val winningTeamIndex = squares.indexOfFirst { it.hasClass("mod-win") }

                        // Find winning team and side
                        // Determine winner
                        val winner = when (winningTeamIndex) {
                            0 -> "team1"
                            1 -> "team2"
                            else -> "unknown"
                        }
                        
                        val winningSide = when {
                            winSq?.hasClass("mod-t") == true -> "t"
                            winSq?.hasClass("mod-ct") == true -> "ct"
                            else -> "unknown"
                        }
                        
                        // Determine sides for both teams
                        // If team1 won on T, team1 is T, team2 is CT
                        // If team2 won on CT, team1 is T, team2 is CT
                        val (tTeam, ctTeam) = when {
                            winner == "team1" && winningSide == "t" -> "team1" to "team2"
                            winner == "team1" && winningSide == "ct" -> "team2" to "team1"
                            winner == "team2" && winningSide == "t" -> "team2" to "team1"
                            winner == "team2" && winningSide == "ct" -> "team1" to "team2"
                            else -> "unknown" to "unknown"
                        }
                        
                        // Extract win type from icon
                        val winIcon = winSq?.select("img")?.attr("src") ?: ""
                        val winIconUrl = when {
                            winIcon.isEmpty() -> null
                            winIcon.startsWith("//") -> "https:$winIcon"
                            winIcon.startsWith("/") -> "https://www.vlr.gg$winIcon"
                            else -> winIcon
                        }
                        
                        val winType = when {
                            winIcon.contains("elim") -> "elimination"
                            winIcon.contains("boom") -> "bomb_exploded"
                            winIcon.contains("defuse") -> "bomb_defused"
                            winIcon.contains("time") -> "time_ran_out"
                            else -> "unknown"
                        }
                        
                        if (roundNum.isNotEmpty() && winningTeamIndex >= 0) {
                            rounds.add(Round(roundNum, score, winner, tTeam, ctTeam, winType, winIconUrl))
                        }
                    }
                }
            }
            
            if (statsForThisMap.isNotEmpty()) {
                mapDataList.add(MapStatsData(mapName, statsForThisMap, rounds))
            }
        }
        
        // Organize into allMaps, map1, map2, etc.
        var allMapsData: MapStatsData? = null
        var map1Data: MapStatsData? = null
        var map2Data: MapStatsData? = null
        var map3Data: MapStatsData? = null
        var map4Data: MapStatsData? = null
        var map5Data: MapStatsData? = null
        
        for ((index, mapData) in mapDataList.withIndex()) {
            when {
                mapData.mapName.equals("All Maps", ignoreCase = true) -> allMapsData = mapData
                index == 0 && allMapsData == null -> map1Data = mapData
                index == 1 || (index == 0 && allMapsData != null) -> if (map1Data == null) map1Data = mapData else map2Data = mapData
                index == 2 -> map2Data = mapData
                index == 3 -> map3Data = mapData
                index == 4 -> map4Data = mapData
                index == 5 -> map5Data = mapData
            }
        }
        
        // Better logic: separate "All Maps" from numbered maps
        allMapsData = mapDataList.find { it.mapName.equals("All Maps", ignoreCase = true) }
        val individualMaps = mapDataList.filter { !it.mapName.equals("All Maps", ignoreCase = true) }
        map1Data = individualMaps.getOrNull(0)
        map2Data = individualMaps.getOrNull(1)
        map3Data = individualMaps.getOrNull(2)
        map4Data = individualMaps.getOrNull(3)
        map5Data = individualMaps.getOrNull(4)
        
        return MatchDetail(
            eventName = eventName,
            eventImage = eventImage,
            matchSubtitle = matchSubtitle,
            date = date,
            time = time,
            patch = patch,
            team1 = TeamInfo(team1Name, team1Logo, team1Url, team1Score),
            team2 = TeamInfo(team2Name, team2Logo, team2Url, team2Score),
            status = status,
            timeUntilMatch = timeUntilMatch,
            format = format,
            bansPicksInfo = bansPicksInfo,
            streams = streams,
            maps = maps,
            mapStats = MapStats(
                allMaps = allMapsData,
                map1 = map1Data,
                map2 = map2Data,
                map3 = map3Data,
                map4 = map4Data,
                map5 = map5Data
            )
        )
    }
    

}
