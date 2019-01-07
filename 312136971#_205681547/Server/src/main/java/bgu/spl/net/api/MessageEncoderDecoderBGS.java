package bgu.spl.net.api;

import bgu.spl.net.api.Messages.*;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class MessageEncoderDecoderBGS implements MessageEncoderDecoder<BGSMessage> {

    private List<Byte> bytes = new LinkedList<>();
    private OpCodesEnum opCode;
    private int bytesCounter = 0;
    private BGSMessage msg;

    public BGSMessage decodeNextByte(byte nextByte) {
        if (bytesCounter == 0) {
            bytes.add(nextByte);
            bytesCounter++;
            return null;
        }
        if (bytesCounter == 1) {
            bytes.add(nextByte);
            opCode = decodeOpCode();
            if (opCode == OpCodesEnum.LOGOUT || opCode == OpCodesEnum.USERLIST)
                return msg;
            bytesCounter++;
            return null;
        }
        switch (opCode) {
            case REGISTER:
                return decodeNextByteRegister(nextByte);
            case LOGIN:
                return decodeNextByteLogin(nextByte);
            case FOLLOW:
                return decodeNextByteFollow(nextByte);
            case POST:
                return decodeNextBytePost(nextByte);
            case PM:
                return decodeNextBytePM(nextByte);
            case STAT:
                return decodeNextByteStat(nextByte);
            default:
                return null;       //TODO change default return
        }
    }

    // return an enum according to the OP Code
    private OpCodesEnum decodeOpCode() {
        byte[] primitiveArr = getPrimitiveBytes(bytes);   // converting the ByteArray to a byteArray
        short opCode = bytesToShort(primitiveArr);           // getting the opCode
        switch (opCode) {
            case 1:
                msg = new RegisterMessage();
                return OpCodesEnum.REGISTER;
            case 2:
                msg = new LoginMessage();
                return OpCodesEnum.LOGIN;
            case 3:
                msg = new LogoutMessage();
                return OpCodesEnum.LOGOUT;
            case 4:
                msg = new FollowMessage();
                return OpCodesEnum.FOLLOW;
            case 5:
                msg = new PostMessage();
                return OpCodesEnum.POST;
            case 6:
                msg = new PMMessage();
                return OpCodesEnum.PM;
            case 7:
                msg = new UserlistMessage();
                return OpCodesEnum.USERLIST;
            case 8:
                msg = new StatMessage();
                return OpCodesEnum.STAT;
            default:
                return null;       // TODO change default msg
        }
    }

    // ----------------------------  Decoders  -------------------------------------------
    //      Register
    private BGSMessage decodeNextByteRegister(byte nextByte) {
        if (((RegisterMessage) msg).getParamCounter() < 2) {
            if (nextByte == '\0')
                ((RegisterMessage) msg).increaseCounter();
            bytes.add(nextByte);
            return null;
        } else {
            byte[] bytesArray = getPrimitiveBytes(bytes);
            // getting username
            int i = 2;
            while (bytes.get(i) != '\0') {
                i++;
            }
            String userName = new String(bytesArray, 2, i - 2, StandardCharsets.UTF_8);
            // getting password
            int j = i + 1;
            while (bytes.get(j) != '\0') {
                j++;
            }
            String password = new String(bytesArray, i + 1, j - (i + 1), StandardCharsets.UTF_8);

            ((RegisterMessage) msg).setUserName(userName);
            ((RegisterMessage) msg).setPassword(password);
            // clearing
            bytes.clear();
            bytesCounter = 0;
            return msg;
        }
    }

    //      Login
    private BGSMessage decodeNextByteLogin(byte nextByte) {
        if (((LoginMessage) msg).getParamCounter() < 2) {
            if (nextByte == '\0')
                ((LoginMessage) msg).increaseCounter();
            bytes.add(nextByte);
            return null;
        } else {
            byte[] bytesArray = getPrimitiveBytes(bytes);
            // getting username
            int i = 2;
            while (bytes.get(i) != '\0') i++;
            String userName = new String(bytesArray, 2, i - 2, StandardCharsets.UTF_8);
            // getting password
            int j = i + 1;
            while (bytes.get(j) != '\0') j++;
            String password = new String(bytesArray, i + 1, j - (i + 1), StandardCharsets.UTF_8);

            ((LoginMessage) msg).setUserName(userName);
            ((LoginMessage) msg).setPassword(password);
            // clearing
            bytes.clear();
            bytesCounter = 0;
            return msg;
        }
    }

    //      Follow
    private BGSMessage decodeNextByteFollow(byte nextByte) {
        // handles follow byte
        if (bytesCounter == 2) {
            bytes.add(nextByte);
            bytesCounter++;
            byte[] primitiveArr = getPrimitiveBytes(bytes.subList(2, 3));   // converting the ByteArray to a byteArray
            ((FollowMessage) msg).setFollow(bytesToShort(primitiveArr) == 0);   // true for follow, false for unfollow
            return null;
        }
        // handles third byte
        if (bytesCounter == 3) {
            bytes.add(nextByte);
            bytesCounter++;
            return null;
        }
        // handles fourth byte, now we know num of users
        if (bytesCounter == 4) {
            bytes.add(nextByte);
            bytesCounter++;
            byte[] primitiveArr = getPrimitiveBytes(bytes.subList(4, 5));    // converting the ByteArray to a byteArray
            ((FollowMessage) msg).setNumOfUsers(bytesToShort(primitiveArr)); //  number of users to follow/unfollow
            return null;
        }
        // handles the content of the user list, inserts the users into a list
        if (bytesCounter > 4) {
            if (nextByte == '\0') {
                List<Byte> currentUser = bytes.subList(((FollowMessage) msg).getLastZeroIndex() + 1, bytesCounter);
                byte[] primitiveArr = getPrimitiveBytes(currentUser);
                String user = new String(primitiveArr, 0, primitiveArr.length, StandardCharsets.UTF_8);
                ((FollowMessage) msg).addUser(user);
                ((FollowMessage) msg).setLastZeroIndex(bytesCounter);
                ((FollowMessage) msg).increaseNumOfZeros();
                if (((FollowMessage) msg).getNumOfZeros() == ((FollowMessage) msg).getNumOfUsers()) {
                    bytes.clear();
                    bytesCounter = 0;
                    return msg;
                } else
                    return null;
            } else {
                bytes.add(nextByte);
                bytesCounter++;
                return null;
            }
        }
        return null;
    }

    //      Post
    private BGSMessage decodeNextBytePost(byte nextByte) {
        if (nextByte != '\0') {
            bytes.add(nextByte);
            return null;
        } else {
            // getting post content
            byte[] bytesArray = getPrimitiveBytes(bytes);
            String post = new String(bytesArray, 0, bytesArray.length, StandardCharsets.UTF_8);
            ((PostMessage) msg).setPost(post);
            // clearing
            bytesCounter = 0;
            bytes.clear();
            return msg;
        }
    }

    //      PM
    private BGSMessage decodeNextBytePM(byte nextByte) {
        if (((PMMessage) msg).getParamCounter() < 2) {
            if (nextByte == '\0')
                ((PMMessage) msg).increaseCounter();
            bytes.add(nextByte);
            return null;
        } else {
            byte[] bytesArray = getPrimitiveBytes(bytes);
            // getting username to send to
            int i = 2;
            while (bytes.get(i) != '\0') i++;
            String userName = new String(bytesArray, 2, i - 2, StandardCharsets.UTF_8);
            // getting content of PM
            int j = i + 1;
            while (bytes.get(j) != '\0') j++;
            String content = new String(bytesArray, i + 1, j - (i + 1), StandardCharsets.UTF_8);

            ((PMMessage) msg).setUserName(userName);
            ((PMMessage) msg).setContent(content);
            // clearing
            bytesCounter = 0;
            bytes.clear();
            return msg;
        }
    }

    //      Stat
    private BGSMessage decodeNextByteStat(byte nextByte) {
        if (nextByte == '\0') {
            byte[] primitiveArr = getPrimitiveBytes(bytes.subList(2, bytes.size()));
            String user = new String(primitiveArr, 0, primitiveArr.length, StandardCharsets.UTF_8);
            ((StatMessage) msg).setUser(user);
            return msg;
        } else {
            bytes.add(nextByte);
            return null;
        }

    }

    public byte[] encode(BGSMessage message) {
        switch (message.getOpCode()) {
            case 9: return encodeNotification(message);
            case 10: return encodeACK(message);
            case 11: return encodeError(message);
            default: return null;       // TODO check what is the default
        }
    }
    // ----------------------------------  Encoders  -------------------------------------
    //      Notification
    private byte[] encodeNotification(BGSMessage message) {
        List<Byte> bytesList = new LinkedList<>(getNonPrimitiveBytes(shortToBytes((short) 11)));
        bytesList.add((byte)((NotificationMessage)message).getType());

        String postingUser = ((NotificationMessage)message).getPostingUser();
        bytesList.addAll(getNonPrimitiveBytes(postingUser.getBytes()));
        bytesList.add((byte)'\0');  // TODO check if good char-to-byte transformation

        String content = ((NotificationMessage)message).getContent();
        bytesList.addAll(getNonPrimitiveBytes(content.getBytes()));
        bytesList.add((byte)'\0');  // TODO same
        return getPrimitiveBytes(bytesList);
    }
    //      ACK
    private byte[] encodeACK(BGSMessage msg){
        byte[] opBytes = shortToBytes((short) 10);
        byte[] opMessageBytes = shortToBytes( ((ACKMessage)msg).getMessageOpCode());
        byte[] both = combine(opBytes, opMessageBytes);
        String optionalData = ((ACKMessage)msg).getOptionalData();
        if( optionalData == null)
            return both;
        else
        {
            if(((ACKMessage)msg).getMessageOpCode() == 8)
                return getOptionalDataBytesShorts(both, optionalData);
            else
                return getOptionalDataBytesString(both, optionalData);
        }
    }   // return an array of bytes
    private byte[] getOptionalDataBytesShorts(byte[] byteArray, String data) {
        //  adding numPosts bytes
        int firstSpaceIndex = data.indexOf(' ');
        String numOfPostsStr = data.substring(0, firstSpaceIndex);
        short numOfPosts = Short.parseShort(numOfPostsStr);
        byteArray = combine(byteArray, shortToBytes(numOfPosts));
        //  adding numFollowers bytes
        int secondsSpaceIndex = data.indexOf(' ',firstSpaceIndex+1);
        String numFollowersStr = data.substring(firstSpaceIndex+1, secondsSpaceIndex);
        short numFollowers = Short.parseShort(numFollowersStr);
        byteArray = combine(byteArray, shortToBytes(numFollowers));
        //  adding numFollowing bytes
        String numFollowingStr = data.substring(secondsSpaceIndex+1);
        short numFollowing = Short.parseShort(numFollowingStr);

        return combine(byteArray, shortToBytes(numFollowing));
    }
    private byte[] getOptionalDataBytesString(byte[] byteArray, String data) {
        //  adding numOfUsers bytes
        int firstSpaceIndex = data.indexOf(' ');
        String numOfUsersStr = data.substring(0, firstSpaceIndex);
        short numOfUsers = Short.parseShort(numOfUsersStr);
        byteArray = combine(byteArray, shortToBytes(numOfUsers));
        //  adding string bytes
        String userNameList = data.substring(firstSpaceIndex);
        int lastIndex = userNameList.indexOf(' ');
        LinkedList<Byte> users = new LinkedList<>();
        while(lastIndex!=-1)
        {
            String user = userNameList.substring(lastIndex, userNameList.indexOf(' ', lastIndex+1));
            users.addAll(getNonPrimitiveBytes(user.getBytes()));
            users.add((byte)'\0');
            lastIndex = userNameList.indexOf(' ', lastIndex+1);
        }
        return combine(byteArray, getPrimitiveBytes(users));
    }
        //      Error
    private byte[] encodeError(BGSMessage message) {
        byte[] opCode = shortToBytes(message.getOpCode());
        byte[] msgOpCode = shortToBytes(((ErrorMessage)message).getMsgOpCode());
        return combine(opCode, msgOpCode);
    }

    //  ---------------------------  Private Methods  ------------------------------------
    // combines two arrays
    private byte[] combine(byte[] a, byte[] b){
        byte[] ret = new byte[a.length+b.length];
        int i=0;
        for(; i<a.length; i++)
            ret[i] = a[i];
        for(int j=0; j<b.length; j++)
            ret[i+j] = b[j];
        return ret;
    }
    private short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    // transforms list of Bytes to array of bytes
    private byte[] getPrimitiveBytes(List<Byte> bytesList) {
        byte[] primitiveArr = new byte[bytesList.size()];
        for (int i = 0; i < bytesList.size(); i++)
            primitiveArr[i] = bytesList.get(i);
        return primitiveArr;
    }
    // transforms a byte array to a list of Bytes
    private List<Byte> getNonPrimitiveBytes(byte[] bytesArr){
        List<Byte> nPBytes= new LinkedList<>();
        for (int i=0; i<bytesArr.length; i++) {
            nPBytes.add(bytesArr[i]);
        }
        return nPBytes;
    }

}