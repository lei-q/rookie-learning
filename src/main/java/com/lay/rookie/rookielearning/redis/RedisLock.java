//���Գ�ȡ������
        String lockUniquePrefix = "lock";

        Jedis jedis = getJedis();
        //1. ��ȡ��
        // key:"lock"+������,value:ʱ���
        //NX -- Only set the key if it does not already exist.
        String key = lockUniquePrefix + Thread.currentThread().getStackTrace()[1].getMethodName();
        Integer userId = 222;
        String conventionK = requestSafeThreadParamDto.getCid();
        //��ͬ�������û�ͬʱ�ύ����,����������,���������Ӧ��ʹ��������������,
        //��������ʹ�÷ֲ�ʽ�����Ƶ�ֻ�� ����������Ϣ��ȫ��ͬ����������,
        //�������������ȫ��ͬ�������ԭ��,���������翨�ٵ����û��ظ����,����nginx ���ط�
        String hashSource = WebServletUtil.buildHashSource(request, userId, conventionK);
        //��������Ϣ ��hash
        long crc32Long = EncryptionUtil.getHash(hashSource);
        //"OK":�ɹ�;null:ʧ��
        String result = jedis.set(key + crc32Long, "aa", "NX", "EX"/*seconds*/, 1000);
        Const.pool.returnResource(jedis);
        boolean success = "OK".equals(result);
        System.out.println("success :" + success);
        System.out.println("result :" + result);

        try {
            if (success) {
                //2. ִ�о���ҵ���߼�
                //...
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3. ҵ���߼�ִ�����֮��,�ͷ���
            if (null != del && del) {
                jedis = getJedis();
                jedis.del(key);
                Const.pool.returnResource(jedis);
            }
        }



//        return new BaseResponseDto(true).setValue(result).toJson();
        return BaseResponseDto.put2("result", result).put("success", success).toJson();