from flie.utils.SimpleTree import Formula





def main():

    f = Formula([encodingConstants.LAND,
                 Formula("p"),
                 Formula([encodingConstants.IMPLIES,
                         Formula("p"),
                         Formula("p")
                    ])
                 ])

    normalized = Formula.normalize(f)


if __name__ == '__main__':
    main()