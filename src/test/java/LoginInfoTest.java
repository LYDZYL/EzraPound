import entity.LoginInfo;
import entity.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import util.JedisUtil;
import util.JsoupUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class LoginInfoTest {
    Logger log = LoggerFactory.getLogger(LoginInfoTest.class);
    LoginInfo loginInfo = new LoginInfo();

    @Test
    public void getXsrfTest() {
        loginInfo.getXsrf(0);
    }

    @Test
    public void getCaptchaTest() {
        loginInfo.getCaptchaImgAndCookies(0);
    }


    public static void main(String[] args) {
        new LoginInfo().login();
    }

    @Test
    public void followingsTest() {
        Jedis jedis = JedisUtil.getJedis();
        loginInfo.login();
        Connection con = JsoupUtil.getGetCon("https://www.zhihu.com/people/lin-shen-shi-jian-lu/followees");
        Response rs = null;
        try {
            rs = con.cookies(loginInfo.getLoginCookies())
                    .cookie("_xsrf", loginInfo.getXsrf())
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(rs.body());
        Element elmt = doc.select(".zh-general-list").first();
        if (elmt == null)
            return;
        JSONObject jsonObject = new JSONObject(elmt.attr("data-init"));
        String param = jsonObject.getJSONObject("params").toString();
        log.info(param);
        con = JsoupUtil.getPostCon("https://www.zhihu.com/node/ProfileFolloweesListV2");
        try {
            rs = con.cookies(loginInfo.getLoginCookies())
                    .ignoreContentType(true)
                    .cookie("_xsrf", loginInfo.getXsrf())
                    .data("method", "next")
                    .data("_xsrf", loginInfo.getXsrf())
                    .data("params", param)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject1 = new JSONObject(rs.body());
        JSONArray jsonArray = jsonObject1.getJSONArray("msg");
        for (int i = 0; i < jsonArray.length(); i++) {
            Document doc1 = Jsoup.parse(jsonArray.get(i).toString());
            String name = doc1.select(".zm-list-content-title a").first().attr("title");
            String shortUrl = doc1.select(".zm-list-content-title a").first().attr("href").replaceAll("https://www.zhihu.com/people/", "");
            log.info(name + shortUrl);
            jedis.sadd("filter", shortUrl);
        }
    }

    @Test
    public void JsonTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("offset", "0");
        jsonObject.put("order_by", "created");
        log.info(jsonObject.toString());
        log.info("jiao-wai-mai-de-mao");
    }

    @Test
    public void getUserInfo() {
        loginInfo.login();
        String userId = "iCheez";
        Connection con = JsoupUtil.getGetCon("https://www.zhihu.com/people/" + userId);
        Response rs = null;
        try {
            rs = con.cookies(loginInfo.getLoginCookies()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(rs.body());
        String name = doc.select(".zm-profile-header-main .name").text();
        String bio = doc.select(".zm-profile-header-main .bio").text();
        String location = doc.select(".zm-profile-header-main .location").text();
        String business = doc.select(".zm-profile-header-main .business").text();
        String gender = doc.select(".zm-profile-header-main .gender i").hasClass("icon-profile-male") ? "男" : "女";
        String education = doc.select(".zm-profile-header-main .education").text();
        String educationExtra = doc.select(".zm-profile-header-main .education-extra").text();
        String description = doc.select(".zm-profile-header-main .content").text();
        String agree = doc.select(".zm-profile-header-user-agree strong").text();
        String thanks = doc.select(".zm-profile-header-user-thanks strong").text();
        String asks = doc.select(".profile-navbar a .num").eq(0).text();
        String answers = doc.select(".profile-navbar a .num").eq(1).text();
        String posts = doc.select(".profile-navbar a .num").eq(2).text();
        String collections = doc.select(".profile-navbar a .num").eq(3).text();
        String logs = doc.select(".profile-navbar a .num").eq(4).text();
        String following = doc.select(".zm-profile-side-following strong").eq(0).text();
        String followers = doc.select(".zm-profile-side-following strong").eq(1).text();
        String hashId=doc.select(".zm-profile-header-op-btns button").attr("data-id");
        User user = new User(userId, name, bio, location, business, gender, education, educationExtra,
                description, agree, thanks, asks, answers, posts, collections, logs, following, followers,hashId);
        log.info(user.toString());
    }
    @Test
    public void getFollowingInfo(){
        loginInfo.login();

    }
}