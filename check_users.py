import psycopg2

try:
    # 连接到数据库
    conn = psycopg2.connect(
        host="localhost",
        database="testplatform",
        user="postgres",
        password="root"
    )

    # 创建游标
    cur = conn.cursor()

    # 查询用户数据
    cur.execute("SELECT * FROM users;")
    rows = cur.fetchall()

    # 打印结果
    for row in rows:
        print("User data:", row)

    # 关闭连接
    cur.close()
    conn.close()
    
    print("Database query completed successfully")
except Exception as e:
    print(f"Error: {e}")
    
    # 尝试使用root密码
    try:
        conn = psycopg2.connect(
            host="localhost",
            database="testplatform",
            user="postgres",
            password="root"
        )

        # 创建游标
        cur = conn.cursor()

        # 查询用户数据
        cur.execute("SELECT * FROM users;")
        rows = cur.fetchall()

        # 打印结果
        for row in rows:
            print(row)

        # 关闭连接
        cur.close()
        conn.close()
    except Exception as e2:
        print(f"Error with root password: {e2}")