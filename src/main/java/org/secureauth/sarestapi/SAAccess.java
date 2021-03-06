package org.secureauth.sarestapi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.secureauth.sarestapi.data.*;
import org.secureauth.sarestapi.data.BehavioralBio.BehaveBioRequest;
import org.secureauth.sarestapi.data.Requests.BehaveBioResetRequest;
import org.secureauth.sarestapi.data.Response.BehaveBioResponse;
import org.secureauth.sarestapi.data.Requests.*;
import org.secureauth.sarestapi.data.Response.*;
import org.secureauth.sarestapi.data.Requests.UserPasswordRequest;
import org.secureauth.sarestapi.data.Response.UserProfileResponse;
import org.secureauth.sarestapi.data.UserProfile.NewUserProfile;
import org.secureauth.sarestapi.data.UserProfile.UserProfile;
import org.secureauth.sarestapi.data.UserProfile.UserToGroups;
import org.secureauth.sarestapi.data.UserProfile.UsersToGroup;
import org.secureauth.sarestapi.queries.*;
import org.secureauth.sarestapi.resources.SAExecuter;
import org.secureauth.sarestapi.util.JSONUtil;
import org.secureauth.sarestapi.util.RestApiHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rrowcliffe@secureauth.com
 * <p>
 *     SAAccess is a class that allows access to the SecureAuth REST API. The intention is to provide an easy method to access
 *     the Secureauth Authentication Rest Services.
 * </p>
 */

 public class SAAccess {

    private static Logger logger = LoggerFactory.getLogger(SAAccess.class);
    protected SABaseURL saBaseURL;
    protected SAAuth saAuth;
    protected SAExecuter saExecuter;

    /**
     *<p>
     *     Returns a SAAccess Object that can be used to query the SecureAuth Rest API
     *     This should be the default object used when setting up connectivity to the SecureAuth Appliance
     *</p>
     * @param host FQDN of the SecureAuth Appliance
     * @param port The port used to access the web application on the Appliance.
     * @param ssl Use SSL
     * @param realm the Configured Realm that enables the RESTApi
     * @param applicationID The Application ID from the Configured Realm
     * @param applicationKey The Application Key from the Configured Realm
     */
    public SAAccess(String host, String port,boolean ssl, String realm, String applicationID, String applicationKey){
        saBaseURL=new SABaseURL(host,port,ssl);
        saAuth = new SAAuth(applicationID,applicationKey,realm);
        saExecuter=new SAExecuter(saBaseURL);
    }

    /**
     *<p>
     *     Returns a SAAccess Object that can be used to query the SecureAuth Rest API
     *     This should be the default object used when setting up connectivity to the SecureAuth Appliance
     *     This Object will allow users to support selfSigned Certificates
     *</p>
     * @param host FQDN of the SecureAuth Appliance
     * @param port The port used to access the web application on the Appliance.
     * @param ssl Use SSL
     * @param selfSigned  Support for SeflSigned Certificates. Setting to enable disable self signed cert support
     * @param realm the Configured Realm that enables the RESTApi
     * @param applicationID The Application ID from the Configured Realm
     * @param applicationKey The Application Key from the Configured Realm
     */
    public SAAccess(String host, String port,boolean ssl,boolean selfSigned, String realm, String applicationID, String applicationKey){
        saBaseURL=new SABaseURL(host,port,ssl,selfSigned);
        saAuth = new SAAuth(applicationID,applicationKey,realm);
        saExecuter=new SAExecuter(saBaseURL);
    }

    /**
     * <p>
     *     Returns IP Risk Evaluation from the Rest API
     * </p>
     * @param userid The User ID that you want to validate from
     * @param ip_address The IP Address of the user making the request for access
     * @return {@link org.secureauth.sarestapi.data.IPEval}
     *
     */
    public IPEval iPEvaluation(String userid, String ip_address){
        String ts = getServerTime();
        RestApiHeader restApiHeader =new RestApiHeader();
        IPEvalRequest ipEvalRequest =new IPEvalRequest();
        ipEvalRequest.setIp_address(ip_address);
        ipEvalRequest.setUser_id(userid);
        ipEvalRequest.setType("risk");

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", IPEvalQuery.queryIPEval(saAuth.getRealm()), ipEvalRequest, ts);

        try{

            return saExecuter.executeIPEval(header,saBaseURL.getApplianceURL() + IPEvalQuery.queryIPEval(saAuth.getRealm()),ipEvalRequest,ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }

        return null;
    }

    /**
     * <p>
     *     Returns the list of Factors available for the specified user
     * </p>
     * @param userid the userid of the identity you wish to have a list of possible second factors
     * @return {@link FactorsResponse}
     */
    public FactorsResponse factorsByUser(String userid){
//    	userid = encode(userid);
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"GET",FactorsQuery.queryFactors(saAuth.getRealm(),userid),ts);


        try{
            return saExecuter.executeGetRequest(header,saBaseURL.getApplianceURL() + FactorsQuery.queryFactors(saAuth.getRealm(),userid),ts, FactorsResponse.class);

        }catch (Exception e){
        logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
    }
        return null;
    }

    /**
     * <p>
     *     Send push to accept request asynchronously 
     * </p>
     *
     * @param userid  the user id of the identity
     * @param factor_id the P2A Id to be compared against
     * @param endUserIP The End Users IP Address
     * @param clientCompany The Client Company Name
     * @param clientDescription The Client Description
     * @return {@link FactorsResponse}
     */
    public ResponseObject sendPushToAcceptReq(String userid, String factor_id, String endUserIP, String clientCompany, String clientDescription){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        PushToAcceptRequest req = new PushToAcceptRequest();
        req.setUser_id(userid);
        req.setType("push_accept");
        req.setFactor_id(factor_id);
        PushAcceptDetails pad = new PushAcceptDetails();
        pad.setEnduser_ip(endUserIP);
        if (clientCompany != null) {
        	pad.setCompany_name(clientCompany);
        }
        if (clientDescription != null) {
        	pad.setApplication_description(clientDescription);
        }
        req.setPush_accept_details(pad);
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), req,ts);

        try{
            return saExecuter.executePostRequest(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()), req,ts, ResponseObject.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }
    
    /**
     * <p>
     *     Perform adaptive auth query
     * </p>
     * @param userid the user id of the identity
     * @param endUserIP the IP of requesting client
     * @return {@link FactorsResponse}
     */
    public AdaptiveAuthResponse adaptiveAuthQuery(String userid, String endUserIP){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AdaptiveAuthRequest req = new AdaptiveAuthRequest(userid, endUserIP);
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAAuth(saAuth.getRealm()), req,ts);

        try{
            return saExecuter.executePostRequest(header,saBaseURL.getApplianceURL() + AuthQuery.queryAAuth(saAuth.getRealm()), req, ts, AdaptiveAuthResponse.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }
    
    public PushAcceptStatus queryPushAcceptStatus(String refId){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String getUri = AuthQuery.queryAuth(saAuth.getRealm()) + "/" + refId;
        String header = restApiHeader.getAuthorizationHeader(saAuth,"GET", getUri,ts);

        try{
            return saExecuter.executeGetRequest(header,saBaseURL.getApplianceURL() + getUri,ts, PushAcceptStatus.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }
    


    /**
     *
     * <p>
     *     Checks if the Username exists within the datastore within SecureAuth
     * </p>
     * @param userid the userid of the identity
     * @return {@link ResponseObject}
     */
    public BaseResponse validateUser(String userid){

        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("user_id");

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);


        try{
            return saExecuter.executeValidateUser(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Checks the users password against SecureAuth Datastore
     * </p>
     * @param userid the userid of the identity
     * @param password The password of the user to validate
     * @return {@link ResponseObject}
     */
    public BaseResponse validateUserPassword(String userid, String password){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("password");
        authRequest.setToken(password);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeValidateUserPassword(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Checks the users pin against SecureAuth Datastore
     * </p>
     * @param userid the userid of the identity
     * @param pin The pin of the user to validate
     * @return {@link ResponseObject}
     */
    public BaseResponse validateUserPin(String userid, String pin){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("pin");
        authRequest.setToken(pin);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeValidateUserPin(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Validate the users Answer to a KB Question
     * </p>
     * @param userid the userid of the identity
     * @param answer The answer to the KBA
     * @param factor_id the KB Id to be compared against
     * @return {@link ResponseObject}
     */
    public BaseResponse validateKba(String userid, String answer, String factor_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("kba");
        authRequest.setToken(answer);
        authRequest.setFactor_id(factor_id);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeValidateKba(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     *<p>
     *     Validate the Oath Token
     *</p>
     * @param userid the userid of the identity
     * @param otp The One Time Passcode to validate
     * @param factor_id The Device Identifier
     * @return {@link ResponseObject}
     */
    public BaseResponse validateOath(String userid, String otp, String factor_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("oath");
        authRequest.setToken(otp);
        authRequest.setFactor_id(factor_id);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeValidateOath(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Send One Time Passcode by Phone
     * </p>
     * @param userid the userid of the identity
     * @param factor_id  Phone Property   "Phone1"
     * @return {@link ResponseObject}
     */
    public ResponseObject deliverOTPByPhone(String userid, String factor_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("call");
        authRequest.setFactor_id(factor_id);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeOTPByPhone(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Send One Time Passcode by SMS
     * </p>
     * @param userid the userid of the identity
     * @param factor_id  Phone Property   "Phone1"
     * @return {@link ResponseObject}
     */
    public ResponseObject deliverOTPBySMS(String userid, String factor_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("sms");
        authRequest.setFactor_id(factor_id);
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeOTPBySMS(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Send One Time Passcode by Email
     * </p>
     * @param userid the userid of the identity
     * @param factor_id  Email Property   "Email1"
     * @return {@link ResponseObject}
     */
    public ResponseObject deliverOTPByEmail(String userid, String factor_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("email");
        authRequest.setFactor_id(factor_id);
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeOTPByEmail(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Send One Time Passcode by Push
     * </p>
     * @param userid the userid of the identity
     * @param factor_id  Device Property   "z0y9x87wv6u5t43srq2p1on"
     * @return {@link ResponseObject}
     */
    public ResponseObject deliverOTPByPush(String userid, String factor_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("push");
        authRequest.setFactor_id(factor_id);
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executePostRequest(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts, ResponseObject.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Send One Time Passcode by Helpdesk
     * </p>
     * @param userid the userid of the identity
     * @param factor_id  Help Desk Property   "HelpDesk1"
     * @return {@link ResponseObject}
     */
    public ResponseObject deliverOTPByHelpDesk(String userid, String factor_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        AuthRequest authRequest = new AuthRequest();

        authRequest.setUser_id(userid);
        authRequest.setType("help_desk");
        authRequest.setFactor_id(factor_id);
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AuthQuery.queryAuth(saAuth.getRealm()), authRequest,ts);

        try{
            return saExecuter.executeOTPByHelpDesk(header,saBaseURL.getApplianceURL() + AuthQuery.queryAuth(saAuth.getRealm()),authRequest,ts);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Returns response to Access History Post Rest API
     * </p>
     * @param userid The User ID that you want to validate from
     * @param ip_address The IP Address of the user to be stored in the Datastore for use when evaluating Geo-Velocity
     * @return {@link AccessHistoryRequest}
     *
     */
    public ResponseObject accessHistory(String userid, String ip_address){
        String ts = getServerTime();
        RestApiHeader restApiHeader =new RestApiHeader();
        AccessHistoryRequest accessHistoryRequest =new AccessHistoryRequest();
        accessHistoryRequest.setIp_address(ip_address);
        accessHistoryRequest.setUser_id(userid);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", AccessHistoryQuery.queryAccessHistory(saAuth.getRealm()), accessHistoryRequest, ts);

        try{

            return saExecuter.executeAccessHistory(header,saBaseURL.getApplianceURL() + AccessHistoryQuery.queryAccessHistory(saAuth.getRealm()),accessHistoryRequest,ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }

        return null;
    }

    /**
     * <p>
     *     Confirm the DFP data from Client using the Rest API
     * </p>
     * @param userid The User ID that you want to validate from
     * @param fingerprint_id The ID of the finger print to check against the data store
     * @return {@link DFPConfirmResponse}
     *
     */
      public DFPConfirmResponse DFPConfirm(String userid, String fingerprint_id){
        String ts = getServerTime();
        RestApiHeader restApiHeader =new RestApiHeader();
        DFPConfirmRequest dfpConfirmRequest =new DFPConfirmRequest();
        dfpConfirmRequest.setUser_id(userid);
        dfpConfirmRequest.setFingerprint_id(fingerprint_id);


        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", DFPQuery.queryDFPConfirm(saAuth.getRealm()), dfpConfirmRequest, ts);

        try{

            return saExecuter.executeDFPConfirm(header,saBaseURL.getApplianceURL() + DFPQuery.queryDFPConfirm(saAuth.getRealm()), dfpConfirmRequest, ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }

        return null;
    }

    /**
     * <p>
     *     Validate the DFP data from Client using the Rest API
     * </p>
     * @param userid The User ID that you want to validate from
     * @param host_address The ID of the finger print to check against the data store
     * @param jsonString The JSON String provided by the Java Script
     * @param accept  Accept Value provided by the application to buidl the Digital Finger Print
     * @param accept_charset The accept Charset supplied by the client from the application server
     * @param accept_encoding The accept Encoding supplied by the client from the application server
     * @param accept_language The accepted language by the client supplied by the application server
     * @return {@link DFPValidateResponse}
     *
     */
    public DFPValidateResponse DFPValidateNewFingerprint(String userid, String host_address, String jsonString, String accept, String accept_charset, String accept_encoding, String accept_language){
        String ts = getServerTime();
        RestApiHeader restApiHeader =new RestApiHeader();
        DFPValidateRequest dfpValidateRequest = JSONUtil.getObjectFromJSONString(jsonString);
        dfpValidateRequest.setUser_id(userid);
        dfpValidateRequest.setHost_address(host_address);
        dfpValidateRequest.getFingerprint().setAccept(accept);
        dfpValidateRequest.getFingerprint().setAccept_charset(accept_charset);
        dfpValidateRequest.getFingerprint().setAccept_language(accept_language);
        dfpValidateRequest.getFingerprint().setAccept_encoding(accept_encoding);





        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", DFPQuery.queryDFPValidate(saAuth.getRealm()), dfpValidateRequest, ts);

        try{

            return saExecuter.executeDFPValidate(header,saBaseURL.getApplianceURL() + DFPQuery.queryDFPValidate(saAuth.getRealm()), dfpValidateRequest, ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }

        return null;
    }

    /**
     * <p>
     *     Returns the url for the JavaScript Source for DFP
     * </p>
     * @return {@link JSObjectResponse}
     */
    public JSObjectResponse javaScriptSrc(){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"GET",DFPQuery.queryDFPjs(saAuth.getRealm()),ts);


        try{
            return saExecuter.executeGetJSObject(header,saBaseURL.getApplianceURL() + DFPQuery.queryDFPjs(saAuth.getRealm()),ts, JSObjectResponse.class);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * Start of Behavior Bio Metrics Methods
     *
     */


    /**
     * <p>
     *     Returns the url for the JavaScript Source for BehaveBioMetrics
     * </p>
     * @return {@link JSObjectResponse}
     */
    public JSObjectResponse BehaveBioJSSrc(){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"GET",BehaveBioQuery.queryBehaveBiojs(saAuth.getRealm()),ts);


        try{
            return saExecuter.executeGetJSObject(header,saBaseURL.getApplianceURL() + BehaveBioQuery.queryBehaveBiojs(saAuth.getRealm()),ts, JSObjectResponse.class);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }


    /**
     * <p>
     *     Submit Behave Bio Profile using the Rest API
     * </p>
     * @param userid The User ID that you want to validate from
     * @param behaviorProfile The Behavioral Profile of the user
     * @param hostAddress The IP Address of the user
     * @param userAgent  The Browser User Agent of the user
     *
     * @return {@link BehaveBioResponse}
     *
     */
    public BehaveBioResponse BehaveBioProfileSubmit(String userid, String behaviorProfile, String hostAddress, String userAgent){
        String ts = getServerTime();
        RestApiHeader restApiHeader =new RestApiHeader();
        BehaveBioRequest behaveBioRequest = new BehaveBioRequest();
        behaveBioRequest.setUserId(userid);
        behaveBioRequest.setBehaviorProfile(behaviorProfile);
        behaveBioRequest.setHostAddress(hostAddress);
        behaveBioRequest.setUserAgent(userAgent);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", BehaveBioQuery.queryBehaveBio(saAuth.getRealm()), behaveBioRequest, ts);

        try{

            return saExecuter.executeBehaveBioPost(header,saBaseURL.getApplianceURL() + BehaveBioQuery.queryBehaveBio(saAuth.getRealm()), behaveBioRequest, ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }

        return null;
    }

    /**
     * <p>
     *     Submit Reset Request to Behave Bio Profile using the Rest API
     * </p>
     * @param userid The User ID that you want to validate from
     * @param fieldName The Behavioral FieldName to Reset
     * @param fieldType The Behavioral FieldType to Reset
     * @param deviceType  The Behavioral DeviceType to Reset
     *
     * @return {@link ResponseObject}
     *
     */
    public ResponseObject BehaveBioProfileReset(String userid, String fieldName, String fieldType, String deviceType){
        String ts = getServerTime();
        RestApiHeader restApiHeader =new RestApiHeader();
        BehaveBioResetRequest behaveBioResetRequest = new BehaveBioResetRequest();
        behaveBioResetRequest.setUserId(userid);
        behaveBioResetRequest.setFieldName(fieldName);
        behaveBioResetRequest.setFieldType(fieldType);
        behaveBioResetRequest.setDeviceType(deviceType);

        String header = restApiHeader.getAuthorizationHeader(saAuth,"PUT", BehaveBioQuery.queryBehaveBio(saAuth.getRealm()), behaveBioResetRequest, ts);

        try{

            return saExecuter.executeBehaveBioReset(header,saBaseURL.getApplianceURL() + BehaveBioQuery.queryBehaveBio(saAuth.getRealm()), behaveBioResetRequest, ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }

        return null;
    }

    /**
     * END of Behavior Bio Metrics Methods
     *
     */

    /**
     * Start of IDM Methods
     */

    /**
     * <p>
     *     Creates User / Profile
     * </p>
     * @param newUserProfile The newUserProfile Object
     * @return {@link ResponseObject}
     */
    public ResponseObject createUser(NewUserProfile newUserProfile){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST",IDMQueries.queryUsers(saAuth.getRealm()),newUserProfile,ts);

        /*
        At a minimum creating a user requires UserId and Passowrd
         */
        if(newUserProfile.getUserId() != null && !newUserProfile.getUserId().isEmpty() && newUserProfile.getPassword() != null && !newUserProfile.getPassword().isEmpty()){
            try{
                return saExecuter.executeUserProfileCreateRequest(header,saBaseURL.getApplianceURL() + IDMQueries.queryUsers(saAuth.getRealm()),newUserProfile,ts,ResponseObject.class);

            }catch (Exception e){
                logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
            }
        }
        return null;
    }

    /**
     * <p>
     *     Update User / Profile
     * </p>
     * @param userId the UserID tied to the Profile Object
     * @param userProfile The User'sProfile Object to be updated
     * @return {@link ResponseObject}
     */
    public ResponseObject updateUser(String userId, NewUserProfile userProfile){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"PUT",IDMQueries.queryUserProfile(saAuth.getRealm(),userId),userProfile,ts);


            try{
                return saExecuter.executeUserProfileUpdateRequest(header,
                        saBaseURL.getApplianceURL() + IDMQueries.queryUserProfile(saAuth.getRealm(),userId),
                        userProfile,
                        ts,
                        ResponseObject.class);

            }catch (Exception e){
                logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
            }

        return null;
    }

    /**
     * <p>
     *     Associate User to Group
     * </p>
     * @param userid the user id of the identity
     * @param groupName The Name of the group to associate the user to
     * @return {@link GroupAssociationResponse}
     */
    public ResponseObject addUserToGroup(String userid, String groupName){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", IDMQueries.queryUserToGroup(saAuth.getRealm(),userid,groupName),ts);

        try{
            return saExecuter.executeSingleUserToSingleGroup(header,saBaseURL.getApplianceURL() + IDMQueries.queryUserToGroup(saAuth.getRealm(),userid,groupName), ts, ResponseObject.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Associate Group to Users
     * </p>
     * @param usersToGroup The Users to Group object holding the userIds
     * @param groupName The Name of the group to associate the user to
     * @return {@link GroupAssociationResponse}
     */
    public GroupAssociationResponse addUsersToGroup(UsersToGroup usersToGroup, String groupName){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", IDMQueries.queryGroupToUsers(saAuth.getRealm(),groupName),usersToGroup,ts);

        try{
            return saExecuter.executeGroupToUsersRequest(header,saBaseURL.getApplianceURL() + IDMQueries.queryGroupToUsers(saAuth.getRealm(),groupName), usersToGroup, ts, GroupAssociationResponse.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }


    /**
     * <p>
     *     Associate Group to User
     * </p>
     * @param groupName the Group Name
     * @param userid The userId to associate to the group
     * @return {@link GroupAssociationResponse}
     */
    public GroupAssociationResponse addGroupToUser(String groupName, String userid){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", IDMQueries.queryGroupToUser(saAuth.getRealm(),userid,groupName),ts);

        try{
            return saExecuter.executeSingleGroupToSingleUser(header,saBaseURL.getApplianceURL() + IDMQueries.queryGroupToUser(saAuth.getRealm(),userid,groupName), ts, GroupAssociationResponse.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Associate User To Groups
     * </p>
     * @param userId The UserId we are going to assign Groups to
     * @param userToGroups The UserToGroups Object holding the list of groups to associate to the user
     * @return {@link GroupAssociationResponse}
     */
    public GroupAssociationResponse addUserToGroups(String userId, UserToGroups userToGroups){
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST", IDMQueries.queryUserToGroups(saAuth.getRealm(),userId),userToGroups,ts);

        try{
            return saExecuter.executeUserToGroupsRequest(header,saBaseURL.getApplianceURL() + IDMQueries.queryUserToGroups(saAuth.getRealm(),userId), userToGroups, ts, GroupAssociationResponse.class);
        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Returns the UserProfile for the specified user
     * </p>
     * @param userid the userid of the identity you wish to have a list of possible second factors
     * @return {@link UserProfileResponse}
     */
    public UserProfileResponse getUserProfile(String userid){
        userid = encode(userid);
        String ts = getServerTime();
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"GET",IDMQueries.queryUserProfile(saAuth.getRealm(),userid),ts);


        try{
            return saExecuter.executeGetRequest(header,saBaseURL.getApplianceURL() + IDMQueries.queryUserProfile(saAuth.getRealm(),userid),ts, UserProfileResponse.class);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Administrative Password Reset for the specified user
     * </p>
     * @param userid the userid of the identity you wish to have a list of possible second factors
     * @param password the users new password
     * @return {@link ResponseObject}
     */
    public ResponseObject passwordReset(String userid, String password){
        userid = encode(userid);
        String ts = getServerTime();
        UserPasswordRequest userPasswordRequest = new UserPasswordRequest();
        userPasswordRequest.setPassword(password);
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST",IDMQueries.queryUserResetPwd(saAuth.getRealm(),userid),userPasswordRequest,ts);


        try{
            return saExecuter.executeUserPasswordReset(header,saBaseURL.getApplianceURL() + IDMQueries.queryUserResetPwd(saAuth.getRealm(),userid),userPasswordRequest,ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * <p>
     *     Self Service Password Reset for the specified user
     * </p>
     * @param userid the userid of the identity you wish to have a list of possible second factors
     * @param currentPassword the users Current password
     * @param newPassword the users new Password
     * @return {@link ResponseObject}
     */
    public ResponseObject passwordChange(String userid, String currentPassword, String newPassword){
        userid = encode(userid);
        String ts = getServerTime();
        UserPasswordRequest userPasswordRequest = new UserPasswordRequest();
        userPasswordRequest.setCurrentPassword(currentPassword);
        userPasswordRequest.setNewPassword(newPassword);
        RestApiHeader restApiHeader = new RestApiHeader();
        String header = restApiHeader.getAuthorizationHeader(saAuth,"POST",IDMQueries.queryUserChangePwd(saAuth.getRealm(),userid),userPasswordRequest,ts);


        try{
            return saExecuter.executeUserPasswordChange(header,saBaseURL.getApplianceURL() + IDMQueries.queryUserChangePwd(saAuth.getRealm(),userid),userPasswordRequest,ts);

        }catch (Exception e){
            logger.error(new StringBuilder().append("Exception occurred executing REST query::\n").append(e.getMessage()).append("\n").toString(), e);
        }
        return null;
    }

    /**
     * End of IDM Methods
     */

    /**
     * End of All SA Access methods
     */

    /**
     *
     * Start Helper Methods
     */

    /**
     *
     * @param input The user String to be encoded
     * @return String
     */
    public static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

    String getServerTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    /**
     *
     * End Helper Methods
     */
}
